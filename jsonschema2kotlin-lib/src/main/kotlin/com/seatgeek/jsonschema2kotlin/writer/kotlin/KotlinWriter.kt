package com.seatgeek.jsonschema2kotlin.writer.kotlin

import com.seatgeek.jsonschema2kotlin.Generator
import com.seatgeek.jsonschema2kotlin.util.SchemaType
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
 */
class KotlinWriter(private val config: Generator.Builder.Config) : SchemaModelWriter {
    private val packagePath =
        config.packageName.replace(".", File.separator) + File.separator

    override fun write(sinkFactory: SinkFactory, schema: Schema) {
        val fileName = getRelativeFilePath(schema)
        val sink = sinkFactory.newFile(fileName)

        sink.buffer()
            .write(
                FileSpec.builder(config.packageName, schema.objectClassNameString)
                    .addType(createClass(schema))
                    .build()
                    .toString()
                    .toByteArray()
            )
            .flush()
    }

    // Implementation

    private fun getRelativeFilePath(schema: Schema): String {
        return packagePath + schema.typeName + ".kt"
    }

    private fun createClass(schema: Schema): TypeSpec {
        return if (schema.isEnum) {
            // Enum (enum class)
            createEnumClass(schema)
        } else {

            // Object (data class)
            createDataClass(schema)
        }
    }

    private fun createEnumClass(schema: Schema): TypeSpec {
        schema.requireEnum()

        return TypeSpec.enumBuilder(schema.enumClassName)
            .addKdoc(schema.description ?: "")
            .apply {
                schema.enums
                    // TODO validate what this looks like for non-strings?
                    .filterIsInstance<String>()
                    .map { KotlinUtils.createSafeConstantName(it, config.enumValueNameDecorators) }
                    .forEach(this::addEnumConstant)
            }
            .build()
    }

    private fun createDataClass(
        schema: Schema
    ): TypeSpec {
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
            .associate { it.title to createClass(it) }

        val properties = schema.properties.map {
            val schemaPropertyName = it.key

            Property(
                schemaPropertyName = schemaPropertyName,
                kotlinPropertyName = KotlinUtils.createSafePropertyName(schemaPropertyName, config.propertyNameDecorators),
                schema = it.value,
                required = schema.requiredProperties.contains(schemaPropertyName)
            )
        }.sortedBy { it.kotlinPropertyName }

        return TypeSpec.classBuilder(schema.objectClassName)
            .addModifiers(KModifier.DATA)
            .apply {
                // Write subtypes
                subtypes.values.forEach(this::addType)

                primaryConstructor(
                    FunSpec.constructorBuilder()
                        .apply {
                            properties.forEach { addParameter(it.poetConstructorParameter) }
                        }
                        .build()
                )

                // Write properties
                // These have to be written as properties in order for kotlin poet to register them as vals in the constructor
                properties.forEach {
                    addProperty(it.poetProperty)
                }
            }
            .build()
    }

    // End implementation

    private data class Property(
        val schemaPropertyName: String,
        val kotlinPropertyName: String,
        val schema: Schema,
        val required: Boolean,
    )

    private val Property.poetProperty: PropertySpec
        get() = PropertySpec.builder(
            name = kotlinPropertyName,
            type = schema.typeName.nullable(!required),
            modifiers = listOf()
        )
            .initializer(kotlinPropertyName)
            .build()

    private val Property.poetConstructorParameter: ParameterSpec
        get() = ParameterSpec.builder(
            name = kotlinPropertyName,
            // TODO likely have to resolve subclassing going on here
            type = schema.typeName.nullable(!required),
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

    // Objects
    private val Schema.objectClassNameString: String
        get() {
            requireObject()
            return KotlinUtils.createSafeClassName(this.title, config.classNameDecorators)
        }

    private val Schema.objectClassName: ClassName
        get() {
            requireObject()
            return ClassName(config.packageName, objectClassNameString)
        }

    private fun Schema.requireObject() {
        if (type != SchemaType.OBJECT) {
            throw IllegalStateException("Schema is not an object")
        }
    }

    // Enums
    private val Schema.isEnum: Boolean
        get() = this.enums != null

    private fun Schema.requireEnum() {
        if (!isEnum) {
            throw IllegalStateException("Schema is not an enum")
        }
    }

    private val Schema.enumClassNameString: String
        get() {
            requireEnum()
            return KotlinUtils.createSafeClassName(this.title, config.classNameDecorators)
        }

    private val Schema.enumClassName: ClassName
        get() {
            requireEnum()
            return ClassName(config.packageName, enumClassNameString)
        }

    /**
     * The [TypeName] for use with KotlinPoet based on the JsonSchema defined type info
     *
     * If this is an enum or an object, this will be the class name
     */
    private val Schema.typeName: TypeName
        get() = if (isEnum) {
            enumClassName
        } else {
            when (type) {
                SchemaType.STRING -> String::class.asTypeName()
                SchemaType.OBJECT -> objectClassName
                SchemaType.INTEGER -> Long::class.asTypeName()
                SchemaType.ARRAY -> List::class.asTypeName().parameterizedBy(this.items.typeName)
                SchemaType.BOOLEAN -> Boolean::class.asTypeName()
                SchemaType.NUMBER -> Double::class.asTypeName()
            }
        }
}