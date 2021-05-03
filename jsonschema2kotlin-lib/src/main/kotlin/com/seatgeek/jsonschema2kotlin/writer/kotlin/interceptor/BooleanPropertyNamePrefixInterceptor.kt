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
        return if (schema.type == SchemaType.BOOLEAN) {
            renameProperty(specs) {
                "is" + it[0].toUpperCase() + it.substring(1)
            }
        } else {
            specs
        }
    }
}