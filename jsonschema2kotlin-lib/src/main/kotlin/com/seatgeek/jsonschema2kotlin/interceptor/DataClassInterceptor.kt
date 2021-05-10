package com.seatgeek.jsonschema2kotlin.interceptor

import com.squareup.kotlinpoet.TypeSpec
import net.jimblackler.jsonschemafriend.Schema

/**
 * An interceptor called every time we're generating a new data class
 *
 * Something you could do with this is to add annotations to the class, or custom methods to interact with your own classes/data elsewhere
 */
fun interface DataClassInterceptor {
    fun intercept(schema: Schema, typeSpec: TypeSpec): TypeSpec
}