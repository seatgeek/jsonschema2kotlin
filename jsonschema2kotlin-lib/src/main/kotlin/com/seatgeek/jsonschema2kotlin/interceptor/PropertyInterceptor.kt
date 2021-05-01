package com.seatgeek.jsonschema2kotlin.interceptor

import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import net.jimblackler.jsonschemafriend.Schema

/**
 * Implementations can modify the spec of constructor properties by calling [PropertySpec.toBuilder] & [ParameterSpec.toBuilder], modifying the spec, then
 * returning the final [PropertySpec] and [ParameterSpec] in a [Pair]
 *
 * Some examples:
 *
 * **Change property name**
 *
 * _Note! There is an important connection between the [ParameterSpec] and the [PropertySpec] in the default implementation in order to make the constructor
 * parameters vals which is that the [PropertySpec] must be initialized from the [ParameterSpec] name_
 *
 * ```kotlin
 * return Pair(
 *     propertySpec.toBuilder("newPropertyName")
 *        .build(),
 *     parameterSpec.toBuilder("newPropertyName")
 *        .initializer("newPropertyName")
 *        .build()
 * ```
 *
 * **Add a `SerializedName` annotation (Gson)**
 * ```kotlin
 * return propertySpec.toBuilder()
 *    .addAnnotation(ClassName("com.google.gson.annotations", "SerializedName"))
 *    .build()
 * ```
 */
fun interface PropertyInterceptor {
    fun intercept(schema: Schema, specs: Pair<ParameterSpec, PropertySpec>): Pair<ParameterSpec, PropertySpec>
}