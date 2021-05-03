package com.seatgeek.jsonschema2kotlin.writer.kotlin

import com.seatgeek.jsonschema2kotlin.writer.SchemaModelWriter
import com.seatgeek.jsonschema2kotlin.writer.SinkFactory
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import net.jimblackler.jsonschemafriend.CombinedSchema
import net.jimblackler.jsonschemafriend.Schema
import java.io.File

class KotlinWriter(private val config: Config) : SchemaModelWriter {
    private val packagePath =
        config.packageName.replace(".", File.pathSeparator) + File.pathSeparator

    override fun write(sinkFactory: SinkFactory, schema: Schema) {
        val sink = sinkFactory.newFile(getFullFilePath(schema))
    }

    // Implementation

    private fun getFullFilePath(schema: Schema): String {
        return packagePath + File.separator + schema.className + ".kt"
    }

    private fun createClass(schema: Schema) {
        val combinedSchema = CombinedSchema(schema)


        FileSpec.builder(config.packageName, schema.className)
            .addType(TypeSpec.classBuilder(schema.className)
                .addModifiers(KModifier.DATA)
                .apply {

                }
                .build()
            )

        schema.properties.forEach {

        }
    }

    // End implementation

    private data class Property(
        val schemaName: String,
        val kotlinName: String,
        val schema: Schema,
        val required: Boolean
    )

    data class Config(
        val parcelize: Boolean = true,
        val classNamePrefix: String = "",
        val classNameSuffix: String = "",
        val packageName: String = ""
    )

    private val Schema.className: String
        get() = KotlinUtils.createSafeClassName(
            config.classNamePrefix,
            this.title,
            config.classNameSuffix
        )
}