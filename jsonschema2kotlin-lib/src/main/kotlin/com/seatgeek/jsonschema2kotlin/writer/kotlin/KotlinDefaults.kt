package com.seatgeek.jsonschema2kotlin.writer.kotlin

import com.seatgeek.jsonschema2kotlin.interceptor.DataClassInterceptor
import com.seatgeek.jsonschema2kotlin.interceptor.EnumClassInterceptor
import com.seatgeek.jsonschema2kotlin.interceptor.PropertyInterceptor
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import net.jimblackler.jsonschemafriend.Schema

object KotlinDefaults {
    fun defaultDataClassInterceptors(): List<DataClassInterceptor> = listOf(SafeClassNameInterceptor)

    fun defaultEnumInterceptors(): List<EnumClassInterceptor> = listOf(CommonCaseEnumClassInterceptor)

    fun defaultPropertyInterceptors(): List<PropertyInterceptor> = listOf(SafePropertyNameInterceptor)
}

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

    override fun intercept(schema: Schema, specs: Pair<ParameterSpec, PropertySpec>): Pair<ParameterSpec, PropertySpec> {
        val (paramSpec, propertySpec) = specs

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
                ("^(" + reservedKeywords.joinToString("|") + ")$").toRegex(),
                "\$1_"
            )


        return Pair(
            paramSpec.toBuilder(newPropertyName)
                .build(),
            propertySpec.toBuilder(newPropertyName)
                .initializer(newPropertyName)
                .build()
        )
    }
}

/**
 * Produces class names which should be safe for use with Kotlin
 */
internal object SafeClassNameInterceptor : DataClassInterceptor {
    override fun intercept(schema: Schema, typeSpec: TypeSpec): TypeSpec {
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

        return typeSpec.toBuilder(name = cleanName)
            .build()
    }
}

/**
 * Produces UPPER_SNAKE_CASE names for enum constants
 */
internal object CommonCaseEnumClassInterceptor : EnumClassInterceptor {
    override fun intercept(schema: Schema, typeSpec: TypeSpec): TypeSpec {
        return typeSpec.toBuilder()
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
}