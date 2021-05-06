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
import java.math.BigDecimal

/**
 * Writes the Kotlin language implementation of the models for the given schems provided to [write] as per [SchemaModelWriter]
 *
 * Currently, this supports the following:
 *
 *  - JsonSchema enums of [String] values as `enum class` in Kotlin
 *  - JsonSchema strings as [String] in Kotlin
 *  - JsonSchema integers as [Long] in Kotlin
 *  - JsonSchema boolean as [Boolean] in Kotlin
 *  - JsonSchema number as [java.math.BigDecimal]
 *  - JsonSchema array of any of the other supported types as [List] parameterized by the appropriate type in Kotlin
 *  - JsonSchema object as `data class` in Kotlin with properties of type object or any other supported type
 *  - JsonSchema object where there's no reference schema is a Map<String, T>
 *
 * Notable missing support:
 *  - anyOf
 *  - allOf
 *  - oneOf
 */
class KotlinWriter(private val config: Generator.Builder.Config) : SchemaModelWriter {
    private val schemaRegistry = mutableMapOf<Schema, TypeSpec>()

    private val packagePath =
        config.packageName.replace(".", File.separator) + File.separator

    override fun write(sinkFactory: SinkFactory, schema: Schema) {
        val rootTypeSpec = createClass(schema) ?: throw IllegalStateException("Root JsonSchema MUST be a complex object with properties")
        val fileName = getRelativeFilePathAndName(rootTypeSpec)
        val rootClassName = requireNotNull(rootTypeSpec.name) { "Expecting root value to have a name" }

        sinkFactory.newFile(fileName)
            .buffer()
            .write(
                FileSpec.builder(config.packageName, rootClassName)
                    .apply {
                        schemaRegistry.values.forEach(this::addType)
                    }
                    .build()
                    .toString()
                    .toByteArray()
            )
            .flush()
    }

    // Implementation

    private fun getRelativeFilePathAndName(typeSpec: TypeSpec): String = packagePath + typeSpec.name + ".kt"

    private fun createClass(schema: Schema): TypeSpec? {
        val typeSpec = schemaRegistry[schema] ?: if (schema.isEnum) {
            // Enum (enum class)
            createEnumClass(schema).let {
                config.enumClassInterceptors.foldRight(it) { enumClassInterceptor, acc ->
                    enumClassInterceptor.intercept(schema, acc)
                }
            }
        } else {
            // Object (data class)
            createDataClass(schema)?.let {
                config.dataClassInterceptors.foldRight(it) { dataClassInterceptor, acc ->
                    dataClassInterceptor.intercept(schema, acc)
                }
            }
        }

        if (typeSpec != null) {
            schemaRegistry[schema] = typeSpec
        }

        // The get or put prevents recursion and generating multiple of the same class
        return typeSpec
    }

    private fun createEnumClass(schema: Schema): TypeSpec {
        schema.requireEnum()

        return TypeSpec.enumBuilder(ClassName(config.packageName, schema.title))
            .addKdoc(schema.description ?: "")
            .apply {
                schema.enums
                    // TODO validate what this looks like for non-strings?
                    .filterIsInstance<String>()
                    .forEach(this::addEnumConstant)
            }
            .build()
    }

    private fun createDataClass(schema: Schema): TypeSpec? {
        schema.requireObject()

        // This is only custom types and arrays
        listOfNotNull(schema.items)
            .asSequence()
            .plus(schema.itemsTuple ?: emptyList())
            .plus(schema.additionalItems)

            // Objects
            .plus(schema.properties
                .map { it.value }
                .filter { it.type == SchemaType.OBJECT })

            // allOf/anyOf/oneOf
            .plus(
                (schema.allOf ?: emptyList())
                    .plus(schema.anyOf ?: emptyList())
                    .plus(schema.oneOf ?: emptyList())
                    .distinct()
            )

            // additionalProperties (references)
            .plus(schema.additionalProperties?.takeIf { it.type == SchemaType.OBJECT })

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
            .toList()
            .forEach { createClass(it) }

        val properties = schema.properties.map {
            val schemaPropertyName = it.key
            val propertySchema = it.value

            try {
                Property(
                    schemaPropertyName = schemaPropertyName,
                    idealModelName = propertySchema.title ?: schemaPropertyName,
                    schema = propertySchema,
                    typeName = propertySchema.typeName(config.packageName, schema.requiredProperties.contains(schemaPropertyName)),
                    required = schema.requiredProperties.contains(schemaPropertyName)
                )
            } catch (e: Exception) {
                println(propertySchema)
                throw e
            }
        }.map {

            val parameterSpec = it.createKotlinPoetConstructorParameter()
            val propertySpec = it.createKotlinPoetProperty()

            val specs = config.propertyInterceptors.foldRight(Pair(parameterSpec, propertySpec)) { propertyInterceptor, acc ->
                propertyInterceptor.intercept(it.schema, propertySpec.getJsonName(), acc)
            }

            it to specs
        }.sortedBy {
            it.second.first.name
        }

        if (properties.isNullOrEmpty()) {
            return null
        }

        return TypeSpec.classBuilder(ClassName(config.packageName, schema.title))
            .addModifiers(KModifier.DATA)
            .addKdoc(schema.title ?: "")
            .addKdoc(schema.description ?: "")
            .apply {
                primaryConstructor(
                    FunSpec.constructorBuilder()
                        .apply { properties.forEach { addParameter(it.second.first) } }
                        .build()
                )

                // Write properties
                // These have to be written as properties in order for kotlin poet to register them as vals in the constructor
                properties.forEach {
                    addProperty(it.second.second)
                }
            }
            .build()
    }

    // End implementation

    private data class Property(
        val schemaPropertyName: String,
        val idealModelName: String,
        val schema: Schema,
        val required: Boolean,
        val typeName: TypeName
    )

    private fun Property.createKotlinPoetProperty(): PropertySpec {
        return PropertySpec.builder(
            name = idealModelName,
            type = typeName,
            modifiers = listOf()
        )
            .addJsonName(schemaPropertyName)
            .initializer(idealModelName)
            .build()
    }

    private fun Property.createKotlinPoetConstructorParameter(): ParameterSpec {
        return ParameterSpec.builder(
            name = idealModelName,
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

    private fun Schema.fqDataClassName(packageName: String): ClassName {
        requireObject()
        return ClassName(
            packageName, schemaRegistry[this]?.name
                ?: throw IllegalStateException("Couldn't get name for schema from registry: $this")
        )
    }

    private fun Schema.fqEnumClassName(packageName: String): ClassName {
        requireEnum()
        return ClassName(
            packageName, schemaRegistry[this]?.name
                ?: throw IllegalStateException("Couldn't get name for schema from registry: $this")
        )
    }

    /**
     * The [TypeName] for use with KotlinPoet based on the JsonSchema defined type info
     *
     * If this is an enum or an object, this will be the class name ([ClassName])
     */
    private fun Schema.typeName(packageName: String, isRequired: Boolean): TypeName = when {
        isEnum -> fqEnumClassName(packageName)
        type == SchemaType.OBJECT -> {
            if (additionalProperties == null && !properties.isNullOrEmpty()) {
                fqDataClassName(packageName)
            } else if (!allOf.isNullOrEmpty()) {
                allOf.singleOrNull()
                    ?.fqDataClassName(packageName)
                    ?: throw IllegalArgumentException("Can't currently generate models for allOf with more than a single argument; $this")
            } else {
                // No properties at all, which means this is a "dictionary" (i.e. map)
                Map::class.asTypeName()
                    .parameterizedBy(String::class.asTypeName(), additionalProperties.typeName(packageName, isRequired))
            }
        }
        type == SchemaType.ARRAY -> List::class.asTypeName().parameterizedBy(this.items.typeName(packageName, isRequired))
        else -> primitiveTypeName()
    }.nullable(!isRequired)

    private fun Schema.primitiveTypeName(): TypeName = if (!isEnum) {
        when (type) {
            SchemaType.STRING -> String::class.asTypeName()
            SchemaType.INTEGER -> Long::class.asTypeName()
            SchemaType.BOOLEAN -> Boolean::class.asTypeName()
            SchemaType.NUMBER -> BigDecimal::class.asTypeName()

            SchemaType.OBJECT,
            SchemaType.ARRAY -> null
        }
    } else {
        null
    } ?: throw IllegalStateException("Trying to get primitive type name for a complex object (Array/Object/Enum), $this")

    private fun PropertySpec.Builder.addJsonName(jsonRawName: String) = apply {
        tag(RawPropertyName::class, RawPropertyName(jsonRawName))
    }

    private fun PropertySpec.getJsonName(): String {
        return tag(RawPropertyName::class)?.rawName ?: throw IllegalStateException("Missing Json property name from property")
    }

    data class RawPropertyName(val rawName: String)
}
