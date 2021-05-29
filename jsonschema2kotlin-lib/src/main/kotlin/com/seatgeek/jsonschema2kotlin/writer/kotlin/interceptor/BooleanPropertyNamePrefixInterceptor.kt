package com.seatgeek.jsonschema2kotlin.writer.kotlin.interceptor

import com.seatgeek.jsonschema2kotlin.interceptor.PropertyInterceptor
import com.seatgeek.jsonschema2kotlin.interceptor.PropertyInterceptor.Companion.renameProperty
import com.seatgeek.jsonschema2kotlin.util.SchemaType
import com.seatgeek.jsonschema2kotlin.util.type
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import net.jimblackler.jsonschemafriend.Schema

internal object BooleanPropertyNamePrefixInterceptor : PropertyInterceptor {
    override fun intercept(schema: Schema, jsonPropertyName: String, specs: Pair<ParameterSpec, PropertySpec>): Pair<ParameterSpec, PropertySpec> {
        return if (schema.type == SchemaType.BOOLEAN
            && !specs.first.name.startsWith("is", true)
            && !specs.first.name.startsWith("has", true)
        ) {
            renameProperty(specs) {
                "is" + it.replaceFirstChar(Char::uppercaseChar)
            }
        } else {
            specs
        }
    }
}