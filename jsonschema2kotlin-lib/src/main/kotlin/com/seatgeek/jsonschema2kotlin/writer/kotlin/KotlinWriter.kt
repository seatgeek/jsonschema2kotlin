package com.seatgeek.jsonschema2kotlin.writer.kotlin

import com.seatgeek.jsonschema2kotlin.Generator
import com.seatgeek.jsonschema2kotlin.util.SchemaType
import com.seatgeek.jsonschema2kotlin.util.isEnum
import com.seatgeek.jsonschema2kotlin.util.requireEnum
import com.seatgeek.jsonschema2kotlin.util.requireObject
import com.seatgeek.jsonschema2kotlin.util.type
import com.seatgeek.jsonschema2kotlin.writer.SchemaModelWriter
import com.seatgeek.jsonschema2kotlin.writer.SinkFactory
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import net.jimblackler.jsonschemafriend.Schema
import nullable
import okio.buffer
import java.io.File

/**
 * Writes the Kotlin language implementation of the models for the given schems provided to [write] as per [SchemaModelWriter]
 *
 * Currently, this supports the following:
 *
 *  - JsonSchema enums of [String] values as `enum class` in Kotlin
 *  - JsonSchema strings as [String] in Kotlin
 *  - JsonSchema integers as [Long] in Kotlin
 *  - JsonSchema boolean as [Boolean] in Kotlin
 *  - JsonSchema number as [Double]
 *  - JsonSchema array of any of the other supported types as [List] parameterized by the appropriate type in Kotlin
 *  - JsonSchema object as `data class` in Kotlin with properties of type object or any other supported type
 *
 * Notable missing support:
 *  - anyOf
 *  - allOf
 *  - oneOf
 */
class KotlinWriter(private val config: Generator.Builder.Config) : SchemaModelWriter {
    private val schemaTypeSpecs = mutableMapOf<Schema, TypeSpec>()

    private val packagePath =
        config.packageName.replace(".", File.separator) + File.separator

    override fun write(sinkFactory: SinkFactory, schema: Schema) {
        val rootTypeSpec = createClass(emptyList(), schema)

        val fileName = getRelativeFilePathAndName(rootTypeSpec)

        val rootClassName = requireNotNull(rootTypeSpec.name) { "Expecting root value to have a name" }

        sinkFactory.newFile(fileName)
            .buffer()
            .write(
                FileSpec.builder(config.packageName, rootClassName)
                    .addType(rootTypeSpec)
                    .build()
                    .toString()
                    .toByteArray()
            )
            .flush()
    }

    // Implementation

    private fun getRelativeFilePathAndName(typeSpec: TypeSpec): String {
        return packagePath + typeSpec.name + ".kt"
    }

    private fun createClass(parents: List<ClassName>, schema: Schema): TypeSpec {
        val typeSpec = if (schema.isEnum) {
            // Enum (enum class)
            createEnumClass(parents, schema)
        } else {
            // Object (data class)
            createDataClass(parents, schema)
        }

        registerSchemaTypeSpec(schema, typeSpec)

        return typeSpec
    }

    private fun registerSchemaTypeSpec(schema: Schema, typeSpec: TypeSpec) {
        schemaTypeSpecs[schema] = typeSpec
    }

    private fun createEnumClass(parents: List<ClassName>, schema: Schema): TypeSpec {
        schema.requireEnum()

        return TypeSpec.enumBuilder(schema.fqEnumClassName(config.packageName, parents))
            .addKdoc(schema.description ?: "")
            .apply {
                schema.enums
                    // TODO validate what this looks like for non-strings?
                    .filterIsInstance<String>()
                    .forEach(this::addEnumConstant)
            }
            .build()
            .let {
                config.enumInterceptors.foldRight(it) { enumInterceptor, typeSpec ->
                    enumInterceptor.intercept(schema, typeSpec)
                }
            }
    }

    private fun createDataClass(
        parents: List<ClassName>,
        schema: Schema
    ): TypeSpec {
        schema.requireObject()

        // TODO using title but might need a better name? like the key name from the dependent
        // schema.dependentSchemas ?
        val className = schema.fqDataClassName(config.packageName, parents)

        // This is only custom types and arrays
        val subtypes = listOfNotNull(schema.items)
            .asSequence()
            .plus(schema.itemsTuple ?: emptyList())
            .plus(schema.additionalItems)

            // Objects
            .plus(schema.properties
                .map { it.value }
                .filter { it.type == SchemaType.OBJECT })

            // Arrays
            .plus(schema.properties
                .map { it.value }
                .filter { it.type == SchemaType.ARRAY }
                .map { it.items }
                .filter { it.type == SchemaType.OBJECT })

            // Enums
            .plus(schema.properties
                .map { it.value }
                .filter { it.isEnum })

            .filterNotNull()
            .associate { it.title to createClass(parents.plus(className), it) }

        val properties = schema.properties.map {
            val schemaPropertyName = it.key

            Property(
                schemaPropertyName = schemaPropertyName,
                schema = it.value,
                typeName = schema.typeName(config.packageName, parents.plus(className), schema.requiredProperties.contains(it.key)),
                required = schema.requiredProperties.contains(schemaPropertyName)
            )
        }.map {
            val parameterSpec = it.createKotlinPoetConstructorParameter()
            val propertySpec = it.createKotlinPoetProperty()

            val specs = config.propertyInterceptors.foldRight(Pair(parameterSpec, propertySpec)) { propertyInterceptor, acc ->
                propertyInterceptor.intercept(it.schema, acc)
            }

            it to specs
        }.sortedBy {
            it.second.first.name
        }

        return TypeSpec.classBuilder(className)
            .addModifiers(KModifier.DATA)
            .apply {
                // Write subtypes
                subtypes.values.forEach(this::addType)

                primaryConstructor(
                    FunSpec.constructorBuilder()
                        .apply { properties.forEach { addParameter(it.second.first) } }
                        .build()
                )

                // Write properties
                // These have to be written as properties in order for kotlin poet to register them as vals in the constructor
                properties.forEach { addProperty(it.second.second) }
            }
            .build()
            .let { inTypeSpec ->
                config.classInterceptors.foldRight(inTypeSpec) { dataClassInterceptor, typeSpec ->
                    dataClassInterceptor.intercept(schema, typeSpec)
                }
            }
    }

    // End implementation

    private data class Property(
        val schemaPropertyName: String,
        val schema: Schema,
        val required: Boolean,
        val typeName: TypeName
    )

    private fun Property.createKotlinPoetProperty(): PropertySpec {
        return PropertySpec.builder(
            name = schemaPropertyName,
            type = typeName,
            modifiers = listOf()
        )
            .initializer(schemaPropertyName)
            .build()
    }

    private fun Property.createKotlinPoetConstructorParameter(): ParameterSpec {
        return ParameterSpec.builder(
            name = schemaPropertyName,
            type = typeName,
            modifiers = listOf(KModifier.PUBLIC)
        )
            .apply {
                if (!required) {
                    defaultValue("null")
                }
            }
            .addKdoc(schema.title ?: "")
            .addKdoc(schema.description ?: "")
            .build()
    }

    private fun Schema.fqDataClassName(packageName: String, parents: List<ClassName>): ClassName {
        requireObject()
        // TODO using title but might need a better name? like the key name from the dependent
        return ClassName(packageName, parents.map { it.simpleName }.plus(title))
    }

    private fun Schema.fqEnumClassName(packageName: String, parents: List<ClassName>): ClassName {
        requireEnum()
        // TODO using title but might need a better name? like the key name from the dependent
        return ClassName(packageName, parents.map { it.simpleName }.plus(title))
    }

    /**
     * The [TypeName] for use with KotlinPoet based on the JsonSchema defined type info
     *
     * If this is an enum or an object, this will be the class name
     */
    private fun Schema.typeName(packageName: String, parents: List<ClassName>, isRequired: Boolean): TypeName = when {
        isEnum -> fqEnumClassName(packageName, parents)
        type == SchemaType.OBJECT -> fqDataClassName(packageName, parents)
        type == SchemaType.ARRAY -> List::class.asTypeName().parameterizedBy(this.items.typeName(packageName, parents, isRequired))
        else -> primitiveTypeName()
    }.nullable(!isRequired)

    private fun Schema.primitiveTypeName(): TypeName = if (!isEnum) {
        when (type) {
            SchemaType.STRING -> String::class.asTypeName()
            SchemaType.INTEGER -> Long::class.asTypeName()
            SchemaType.BOOLEAN -> Boolean::class.asTypeName()
            SchemaType.NUMBER -> Double::class.asTypeName()

            SchemaType.OBJECT,
            SchemaType.ARRAY -> null
        }
    } else {
        null
    } ?: throw IllegalStateException("Trying to get primitive type name for a complex object (Array/Object/Enum), $this")
}