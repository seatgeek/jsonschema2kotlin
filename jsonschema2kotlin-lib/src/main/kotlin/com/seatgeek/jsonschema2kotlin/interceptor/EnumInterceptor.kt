package com.seatgeek.jsonschema2kotlin.interceptor

import com.squareup.kotlinpoet.TypeSpec
import net.jimblackler.jsonschemafriend.Schema

fun interface EnumInterceptor {
    fun intercept(schema: Schema, typeSpec: TypeSpec): TypeSpec
}