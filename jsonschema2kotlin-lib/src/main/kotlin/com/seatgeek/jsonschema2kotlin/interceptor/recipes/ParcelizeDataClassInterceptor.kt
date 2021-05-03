package com.seatgeek.jsonschema2kotlin.interceptor.recipes

import com.seatgeek.jsonschema2kotlin.interceptor.DataClassInterceptor
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec
import net.jimblackler.jsonschemafriend.Schema

/**
 * Adds @Parcelize support to data class models, including @Parcelable annotation
 */
object ParcelizeDataClassInterceptor : DataClassInterceptor {
    override fun intercept(schema: Schema, typeSpec: TypeSpec): TypeSpec = typeSpec.toBuilder()
        .addSuperinterface(ClassName("android.os", "Parcelable"))
        .addAnnotation(ClassName("kotlinx.parcelize", "Parcelize"))
        .build()
}