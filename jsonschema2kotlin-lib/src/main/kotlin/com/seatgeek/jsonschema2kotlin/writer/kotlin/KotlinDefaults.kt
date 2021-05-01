package com.seatgeek.jsonschema2kotlin.writer.kotlin

import com.seatgeek.jsonschema2kotlin.interceptor.DataClassInterceptor
import com.seatgeek.jsonschema2kotlin.interceptor.EnumInterceptor
import com.seatgeek.jsonschema2kotlin.interceptor.PropertyInterceptor

object KotlinDefaults {
    fun defaultDataClassInterceptors(): List<DataClassInterceptor> {
        return listOf(
            DataClassInterceptor { _, typeSpec ->
                val className = requireNotNull(typeSpec.name) { "Data classes are all expected to have a name" }
                val cleanName = when {
                    // Name is fine as-is
                    // I _think_ we shouldn't have to worry about reserved keywords given all reserved
                    // keywords are lowercase and these will always be UpperCamelCase
                    className.matches(Regex("^[A-Z][A-Za-z0-9]+$")) -> className

                    // Name needs cleansing
                    else -> {
                        // Remove bad characters
                        className.replace("[^A-Za-z0-9]+".toRegex(), " ")
                            .let {
                                // Uppercases new words' first letter
                                it.toCharArray().mapIndexed { index, c ->
                                    // Uppercase everything after replaced bad chars for UpperCamelCase
                                    if (index == 0 || it[index - 1] == ' ') {
                                        c.toUpperCase()
                                    } else {
                                        c
                                    }
                                }.joinToString("")
                            }.replace(" ", "")
                    }
                }

                typeSpec.toBuilder(name = cleanName)
                    .build()
            }
        )
    }

    fun defaultEnumInterceptors(): List<EnumInterceptor> {
        return listOf(
            EnumInterceptor { _, typeSpec ->
                typeSpec.toBuilder()
                    .apply {
                        val updated = enumConstants.map { (key, typeSpec) ->
                            key.toUpperCase()
                                .replace("[^A-Za-z0-9]+".toRegex(), "_") to typeSpec
                        }

                        enumConstants.clear()

                        enumConstants.putAll(updated)
                    }
                    .build()
            }
        )
    }

    fun defaultPropertyInterceptors(): List<PropertyInterceptor> {
        return listOf(
            PropertyInterceptor { _, (paramSpec, propertySpec) ->
                val newPropertyName = paramSpec.name
                    .replace("[^A-Za-z0-9]".toRegex(), " ")
                    .let {
                        // Uppercases new words' first letter; we don't lowercase the very first character because we don't know what the Interceptors will do
                        it.toCharArray().mapIndexed { index, c ->
                            // Uppercase everything after replaced bad chars for UpperCamelCase
                            if (index == 0 || it[index - 1] == ' ') {
                                c.toUpperCase()
                            } else {
                                c
                            }
                        }.joinToString("")
                    }
                    .replace(" ", "")
                    // Now lowercase first letter
                    .let {
                        it[0].toLowerCase() + it.substring(1)
                    }
                    // Hard keywords from https://www.programiz.com/kotlin-programming/keywords-identifiers
                    // Replaces only if it's exactly and only the word
                    .replace(
                        "^(as|break|class|continue|do|else|false|for|fun|if|in|interface|is|null|object|package|return|super|this|throw|true|try|typealias|typeof|val|var|when|while)$".toRegex(),
                        "\$1_"
                    )


                Pair(
                    paramSpec.toBuilder(newPropertyName)
                        .build(),
                    propertySpec.toBuilder(newPropertyName)
                        .initializer(newPropertyName)
                        .build()
                )
            }
        )
    }
}