package com.seatgeek.jsonschema2kotlin.writer.kotlin.interceptor

import com.seatgeek.jsonschema2kotlin.interceptor.EnumClassInterceptor
import com.squareup.kotlinpoet.TypeSpec
import net.jimblackler.jsonschemafriend.Schema

/**
 * Produces UPPER_SNAKE_CASE names for enum constants
 */
internal object CommonCaseEnumClassInterceptor : EnumClassInterceptor {
    override fun intercept(schema: Schema, typeSpec: TypeSpec): TypeSpec {
        return typeSpec.toBuilder()
            .apply {
                val updated = enumConstants.map { (key, typeSpec) ->
                    key.uppercase().replace("[^A-Za-z0-9]+".toRegex(), "_") to typeSpec
                }

                enumConstants.clear()

                enumConstants.putAll(updated)
            }
            .build()
    }
}