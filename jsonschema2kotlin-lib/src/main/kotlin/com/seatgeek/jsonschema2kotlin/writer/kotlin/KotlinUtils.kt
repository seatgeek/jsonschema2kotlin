package com.seatgeek.jsonschema2kotlin.writer.kotlin

import com.seatgeek.jsonschema2kotlin.StringDecorator

// TODO most if not all of this could be applied via our own default set of decorators
object KotlinUtils {
    fun createSafeClassName(title: String, decorators: List<StringDecorator>): String {
        val cleanName = when {
            // Name is fine as-is
            // I _think_ we shouldn't have to worry about reserved keywords given all reserved
            // keywords are lowercase and these will always be UpperCamelCase
            title.matches(Regex("^[A-Z][A-Za-z0-9]+$")) -> title

            // Name needs cleansing
            else -> {
                // Remove bad characters
                title.replace("[^A-Za-z0-9]+".toRegex(), " ")
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

        var outName = cleanName

        decorators.forEach { outName = it(outName) }

        return outName
    }

    fun createSafePropertyName(schemaPropertyName: String, decorators: List<StringDecorator>): String =
        schemaPropertyName.replace("[^A-Za-z0-9]".toRegex(), " ")
            .let {
                // Uppercases new words' first letter; we don't lowercase the very first character because we don't know what the decorators will do
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
            // Apply decorators
            .let {
                var outname = it
                decorators.forEach { outname = it(outname) }
                outname
            }
            // Now lowercase first letter
            .let {
                it[0].toLowerCase() + it.substring(1)
            }
            // Hard keywords from https://www.programiz.com/kotlin-programming/keywords-identifiers
            // Replaces only if it's exactly and only the word
            .replace("^(as|break|class|continue|do|else|false|for|fun|if|in|interface|is|null|object|package|return|super|this|throw|true|try|typealias|typeof|val|var|when|while)$".toRegex(), "\$1_")

    fun createSafeConstantName(value: String, decorators: List<StringDecorator>): String {
        return value.toUpperCase()
            .replace("[^A-Za-z0-9]+".toRegex(), "_")
    }
}