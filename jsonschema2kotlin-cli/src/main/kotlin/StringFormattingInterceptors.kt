package com.seatgeek.jsonschema2kotlin.app

import com.seatgeek.jsonschema2kotlin.interceptor.DataClassInterceptor
import com.seatgeek.jsonschema2kotlin.interceptor.EnumClassInterceptor
import com.seatgeek.jsonschema2kotlin.interceptor.PropertyInterceptor
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import net.jimblackler.jsonschemafriend.Schema

class StringDataClassNameInterceptors(private val format: String) : DataClassInterceptor {
    override fun intercept(schema: Schema, typeSpec: TypeSpec): TypeSpec = typeSpec.toBuilder(name = format.format(typeSpec.name))
        .build()
}

class StringEnumClassNameInterceptors(private val format: String) : EnumClassInterceptor {
    override fun intercept(schema: Schema, typeSpec: TypeSpec): TypeSpec = typeSpec.toBuilder(name = format.format(typeSpec.name))
        .build()
}

class StringPropertyNameInterceptor(private val format: String) : PropertyInterceptor {
    override fun intercept(schema: Schema, jsonPropertyName: String, specs: Pair<ParameterSpec, PropertySpec>): Pair<ParameterSpec, PropertySpec> {
        val newName = specs.first.name
            .let {
                it.replaceFirstChar(Char::uppercaseChar)
            }
            .let {
                format.format(it)
            }
            .let {
                it.replaceFirstChar(Char::lowercaseChar)
            }

        return Pair(
            specs.first.toBuilder(name = newName)
                .build(),

            specs.second.toBuilder(name = newName)
                .initializer(newName)
                .build()
        )
    }
}