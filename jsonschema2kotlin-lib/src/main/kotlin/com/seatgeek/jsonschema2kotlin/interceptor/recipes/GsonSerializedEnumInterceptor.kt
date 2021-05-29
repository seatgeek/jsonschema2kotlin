package com.seatgeek.jsonschema2kotlin.interceptor.recipes

import com.seatgeek.jsonschema2kotlin.interceptor.EnumConstantInterceptor
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec
import net.jimblackler.jsonschemafriend.Schema

object GsonSerializedEnumInterceptor : EnumConstantInterceptor {
    override fun intercept(schema: Schema, jsonPropertyName: String, constantNameToTypeSpec: Pair<String, TypeSpec>): Pair<String, TypeSpec> {
        val (constant, typeSpec) = constantNameToTypeSpec

        return constant to typeSpec.toBuilder()
            .addAnnotation(
                AnnotationSpec.builder(ClassName("com.google.gson.annotations", "SerializedName"))
                    .addMember("%S", jsonPropertyName)
                    .build()
            )
            .build()
    }
}