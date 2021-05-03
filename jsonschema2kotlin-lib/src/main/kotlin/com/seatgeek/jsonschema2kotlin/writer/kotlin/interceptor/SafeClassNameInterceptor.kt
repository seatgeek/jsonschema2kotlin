package com.seatgeek.jsonschema2kotlin.writer.kotlin.interceptor

import com.seatgeek.jsonschema2kotlin.interceptor.DataClassInterceptor
import com.squareup.kotlinpoet.TypeSpec
import net.jimblackler.jsonschemafriend.Schema

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