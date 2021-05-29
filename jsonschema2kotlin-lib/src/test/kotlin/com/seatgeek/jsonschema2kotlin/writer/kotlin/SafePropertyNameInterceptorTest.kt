package com.seatgeek.jsonschema2kotlin.writer.kotlin

import com.seatgeek.jsonschema2kotlin.writer.kotlin.interceptor.SafePropertyNameInterceptor
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asTypeName
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock

internal class SafePropertyNameInterceptorTest {
    @Test
    fun `reserved keywords are converted to non-reserved keyword property names`() {
        SafePropertyNameInterceptor.reservedKotlinKeywords.forEach { keyword ->
            val paramSpec = ParameterSpec.builder(keyword, String::class.asTypeName()).build()
            val propSpec = PropertySpec.builder(keyword, String::class.asTypeName()).initializer(keyword).build()

            assertEquals(keyword + "_", SafePropertyNameInterceptor.intercept(mock(), "sheesh", Pair(paramSpec, propSpec)).first.name)
            assertEquals(keyword + "_", SafePropertyNameInterceptor.intercept(mock(), "sheesh", Pair(paramSpec, propSpec)).second.name)
            assertEquals(keyword + "_", SafePropertyNameInterceptor.intercept(mock(), "sheesh", Pair(paramSpec, propSpec)).second.initializer.toString())
        }
    }

    @Test
    fun `property names have lowercase first letter`() {
        val propertyName = "Pancakes"
        val paramSpec = ParameterSpec.builder(propertyName, String::class.asTypeName()).build()
        val propSpec = PropertySpec.builder(propertyName, String::class.asTypeName()).initializer(propertyName).build()

        assertEquals("pancakes", SafePropertyNameInterceptor.intercept(mock(), "sheesh", Pair(paramSpec, propSpec)).first.name)
        assertEquals("pancakes", SafePropertyNameInterceptor.intercept(mock(), "sheesh", Pair(paramSpec, propSpec)).second.name)
        assertEquals("pancakes", SafePropertyNameInterceptor.intercept(mock(), "sheesh", Pair(paramSpec, propSpec)).second.initializer.toString())
    }
}