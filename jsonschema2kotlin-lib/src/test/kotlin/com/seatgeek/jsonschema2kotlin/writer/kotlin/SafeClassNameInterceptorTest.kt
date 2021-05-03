package com.seatgeek.jsonschema2kotlin.writer.kotlin

import com.seatgeek.jsonschema2kotlin.writer.kotlin.interceptor.SafeClassNameInterceptor
import com.squareup.kotlinpoet.TypeSpec
import org.junit.Test
import org.mockito.kotlin.mock
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SafeClassNameInterceptorTest {
    @Test
    fun `valid class name returns same class name`() {
        assertEquals("PersonModel", SafeClassNameInterceptor.intercept(mock(), TypeSpec.classBuilder("PersonModel").build()).name)
    }

    @Test
    fun `class with non-alphanumeric splits by non-alphanumeric into words in UpperCamelCase`() {
        assertEquals("Person123ModelA", SafeClassNameInterceptor.intercept(mock(), TypeSpec.classBuilder("Person123-///Model_a").build()).name)
    }
}