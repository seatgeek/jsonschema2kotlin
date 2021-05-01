package com.seatgeek.jsonschema2kotlin.app

import com.seatgeek.jsonschema2kotlin.interceptor.DataClassInterceptor
import com.seatgeek.jsonschema2kotlin.interceptor.PropertyInterceptor
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import net.jimblackler.jsonschemafriend.Schema

class StringClassNameInterceptors(private val format: String) : DataClassInterceptor {
    override fun intercept(schema: Schema, typeSpec: TypeSpec): TypeSpec = typeSpec.toBuilder(name = format.format(typeSpec.name))
        .build()
}

class StringPropertyNameInterceptor(private val format: String) : PropertyInterceptor {
    override fun intercept(schema: Schema, specs: Pair<ParameterSpec, PropertySpec>): Pair<ParameterSpec, PropertySpec> = Pair(
        specs.first.toBuilder(name = format.format(specs.first.name)).build(),
        specs.second.toBuilder(name = format.format(specs.second.name)).build()
    )
}