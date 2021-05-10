package com.seatgeek.jsonschema2kotlin.writer.kotlin.interceptor

import com.seatgeek.jsonschema2kotlin.interceptor.DataClassInterceptor
import com.squareup.kotlinpoet.TypeSpec
import net.jimblackler.jsonschemafriend.Schema

/**
 * Produces class names which should be safe for use with Kotlin
 */
internal object SafeClassNameInterceptor : DataClassInterceptor {

    private val disallowedCharacterRegex = "[^A-Za-z0-9]+".toRegex()

    override fun intercept(schema: Schema, typeSpec: TypeSpec): TypeSpec {
        val className = requireNotNull(typeSpec.name) { "Data classes are all expected to have a name" }
            // Remove bad characters
            .replace(disallowedCharacterRegex, " ")
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


        return typeSpec.toBuilder(name = className)
            .build()
    }
}