package com.seatgeek.jsonschema2kotlin

import com.seatgeek.jsonschema2kotlin.interceptor.DataClassInterceptor
import com.seatgeek.jsonschema2kotlin.interceptor.EnumInterceptor
import com.seatgeek.jsonschema2kotlin.interceptor.PropertyInterceptor
import com.seatgeek.jsonschema2kotlin.writer.SinkFactory
import com.seatgeek.jsonschema2kotlin.writer.kotlin.KotlinDefaults
import com.seatgeek.jsonschema2kotlin.writer.kotlin.KotlinWriter
import net.jimblackler.jsonschemafriend.Schema
import net.jimblackler.jsonschemafriend.SchemaStore
import java.io.File

class Generator private constructor(private val config: Builder.Config) {

    fun generate() {
        val store = SchemaStore()
        val schemas = config.inputs
            .map {
                // Passed files are a series of paths which could be files or directories
                if (it.isFile) {
                    listOf(it)
                } else {
                    it.listFiles()?.toList() ?: emptyList()
                }
            }
            .flatten()
            .map<File, Schema>(store::loadSchema)

        val sinkFactory = buildSinkFactory()
        val kotlinWriter = KotlinWriter(config)

        schemas.forEach {
            kotlinWriter.write(sinkFactory, it)
        }
    }

    private fun buildSinkFactory(): SinkFactory {
        return when (config.output) {
            Output.Stdout -> SinkFactory.stdout()
            is Output.Directory -> SinkFactory.files(config.output.directory)
        }
    }

    class Builder internal constructor(private var config: Config) {

        fun updateConfig(body: (Config) -> Config): Builder {
            this.config = body(config)
            return this
        }

        fun build(): Generator {
            return Generator(config)
        }

        data class Config(
            val inputs: List<File>,
            val output: Output,

            // TODO move these options to Language-level options?
            val packageName: String = "",
            val parcelize: Boolean = false,
            /**
             * Interceptors applied to data classes
             *
             * An example implementation would be prefixing all of the class names with Api and suffixing it with Model
             */
            val classInterceptors: List<DataClassInterceptor> = KotlinDefaults.defaultDataClassInterceptors(),
            /**
             * Interceptors applied to the properties
             */
            val propertyInterceptors: List<PropertyInterceptor> = KotlinDefaults.defaultPropertyInterceptors(),
            /**
             * Interceptors applied to the enum classes
             */
            val enumInterceptors: List<EnumInterceptor> = KotlinDefaults.defaultEnumInterceptors(),
        )
    }

    companion object {
        fun builder(inputs: List<File>, output: Output): Builder {
            return Builder(Builder.Config(inputs, output))
        }
    }
}

data class Input(val paths: List<File>)

sealed class Output {
    data class Directory(val directory: File) : Output()
    object Stdout : Output() {
        const val NAME = "STDOUT"
    }
}

