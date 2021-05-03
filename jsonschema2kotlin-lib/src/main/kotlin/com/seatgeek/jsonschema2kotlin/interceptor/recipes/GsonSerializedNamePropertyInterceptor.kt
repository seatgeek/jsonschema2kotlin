package com.seatgeek.jsonschema2kotlin.interceptor.recipes

import com.seatgeek.jsonschema2kotlin.interceptor.PropertyInterceptor
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import net.jimblackler.jsonschemafriend.Schema

object GsonSerializedNamePropertyInterceptor : PropertyInterceptor {
    override fun intercept(schema: Schema, jsonPropertyName: String, specs: Pair<ParameterSpec, PropertySpec>): Pair<ParameterSpec, PropertySpec> = Pair(
        specs.first.toBuilder()
            .addAnnotation(
                AnnotationSpec.builder(ClassName("com.google.gson.annotations", "SerializedName"))
                    .addMember("%S", jsonPropertyName)
                    .build()
            )
            .build(),
        specs.second
    )
}