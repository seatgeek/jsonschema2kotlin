package com.seatgeek.jsonschema2kotlin.interceptor

import com.squareup.kotlinpoet.TypeSpec
import net.jimblackler.jsonschemafriend.Schema

fun interface EnumClassInterceptor {
    fun intercept(schema: Schema, typeSpec: TypeSpec): TypeSpec
}