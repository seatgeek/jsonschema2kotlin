package com.seatgeek.jsonschema2kotlin

import net.jimblackler.jsonschemafriend.Schema
import net.jimblackler.jsonschemafriend.SchemaStore
import java.io.File

class Generator private constructor(private val config: Builder.Config) {

    fun produce() {
        val store = SchemaStore()
        val schemas = config.inputs
            .map {
                // Passed files are a series of paths which could be files or directories
                if (it.isFile) {
                    listOf(it)
                } else {
                    it.listFiles()!!.toList()
                }
            }
            .flatten()
            .map<File, Schema>(store::loadSchema)

        schemas.forEach {

        }
    }

    class Builder internal constructor(private var config: Config) {

        data class Config(
            val inputs: List<File>,
            val outputDirectory: Output,
            val packageName: String = "",
            val parcelize: Boolean = false,
            val typeAdapters: List<TypeAdapter> = emptyList()
        )

        fun addTypeAdapter(typeAdapter: TypeAdapter): Builder {
            config = config.copy(
                typeAdapters = config.typeAdapters.toMutableList().apply { add(typeAdapter) })
            return this
        }

        fun packageName(name: String): Builder {
            config = config.copy(packageName = name)
            return this
        }

        fun addParcelize(parcelize: Boolean): Builder {
            config = config.copy(parcelize = parcelize)
            return this
        }

        fun build(): Generator {
            return Generator(config)
        }
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
    data class File(val file: java.io.File) : Output()
    data class Directory(val directory: java.io.File) : Output()
    object Stdout : Output() {
        const val NAME = "STDOUT"
    }
}

interface TypeAdapter {
}