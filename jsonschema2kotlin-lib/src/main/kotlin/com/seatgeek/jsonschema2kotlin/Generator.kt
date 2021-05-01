package com.seatgeek.jsonschema2kotlin

import com.seatgeek.jsonschema2kotlin.writer.SinkFactory
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

        // TODO might regret making this only the raw String instead of the full Schema
        fun addClassNameDecorator(decorator: StringDecorator): Builder {
            config = config.copy(classNameDecorators = config.classNameDecorators.plus(decorator))
            return this
        }

        fun addPropertyNameDecorator(decorator: StringDecorator): Builder {
            config = config.copy(propertyNameDecorators = config.propertyNameDecorators.plus(decorator))
            return this
        }

        fun withPackageName(name: String): Builder {
            config = config.copy(packageName = name)
            return this
        }

        fun withParcelize(parcelize: Boolean): Builder {
            config = config.copy(parcelize = parcelize)
            return this
        }

        fun build(): Generator {
            return Generator(config)
        }

        data class Config(
            val inputs: List<File>,
            val output: Output,
            // TODO for something like Gson, we might want to allow Class/Enum decorators that give the TypeSpec

            // TODO move these options to Language-level options?
            val packageName: String = "",
            val parcelize: Boolean = false,
            /**
             * Decorator applied to the class name to decide its final class name
             *
             * An example implementation migth be prefixing all of the class names with Api and suffixing it with Model
             */
            val classNameDecorators: List<StringDecorator> = emptyList(),
            /**
             * Decorator applied to the property name to decide its final class name
             */
            val propertyNameDecorators: List<StringDecorator> = emptyList(),
            /**
             * Decorator applied to the enum class values to decide their final value name
             */
            val enumValueNameDecorators: List<StringDecorator> = emptyList(),
        )
    }

    companion object {
        fun builder(inputs: List<File>, output: Output): Builder {
            return Builder(Builder.Config(inputs, output))
        }
    }
}

sealed class Input {
    data class Paths(val paths: List<File>) : Input()
}

sealed class Output {
    data class Directory(val directory: File) : Output()
    object Stdout : Output() {
        const val NAME = "STDOUT"
    }
}

typealias StringDecorator = (name: String) -> String