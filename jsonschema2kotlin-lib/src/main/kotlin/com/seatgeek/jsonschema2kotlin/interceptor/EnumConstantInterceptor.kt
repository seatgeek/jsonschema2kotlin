package com.seatgeek.jsonschema2kotlin.interceptor

import com.squareup.kotlinpoet.TypeSpec
import net.jimblackler.jsonschemafriend.Schema

fun interface EnumConstantInterceptor {
    /**
     * Provides the opportunity to mutate an enumConstant ([constantNameToTypeSpec], which is the constant name to the type spec) with given [jsonPropertyName]
     * and have the mutated version returned for use
     */
    fun intercept(schema: Schema, jsonPropertyName: String, constantNameToTypeSpec: Pair<String, TypeSpec>): Pair<String, TypeSpec>
}