package com.seatgeek.jsonschema2kotlin.writer.kotlin.interceptor

import com.seatgeek.jsonschema2kotlin.interceptor.PropertyInterceptor
import com.seatgeek.jsonschema2kotlin.interceptor.PropertyInterceptor.Companion.renameProperty
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import net.jimblackler.jsonschemafriend.Schema

/**
 * Produces lowerCamelCase names that should be safe for use with Kotlin
 */
internal object SafePropertyNameInterceptor : PropertyInterceptor {
    internal val reservedKeywords = listOf(
        "as",
        "break",
        "class",
        "continue",
        "do",
        "else",
        "false",
        "for",
        "fun",
        "if",
        "in",
        "interface",
        "is",
        "null",
        "object",
        "package",
        "return",
        "super",
        "this",
        "throw",
        "true",
        "try",
        "typealias",
        "typeof",
        "val",
        "var",
        "when",
        "while"
    )

    override fun intercept(schema: Schema, jsonPropertyName: String, specs: Pair<ParameterSpec, PropertySpec>): Pair<ParameterSpec, PropertySpec> {
        return renameProperty(specs) {
            it.replace("[^A-Za-z0-9]".toRegex(), " ")
                .let { spacedString ->
                    // Uppercases new words' first letter; we don't lowercase the very first character because we don't know what the Interceptors will do
                    spacedString.toCharArray().mapIndexed { index, c ->
                        // Uppercase everything after replaced bad chars for UpperCamelCase
                        if (index == 0 || spacedString[index - 1] == ' ') {
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
                    regex = "^(${reservedKeywords.joinToString("|")})$".toRegex(),
                    replacement = "\$1_"
                )
        }
    }
}