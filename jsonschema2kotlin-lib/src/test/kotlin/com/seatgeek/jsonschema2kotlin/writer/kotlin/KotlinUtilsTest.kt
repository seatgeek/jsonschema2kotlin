package com.seatgeek.jsonschema2kotlin.writer.kotlin

import org.junit.Test
import kotlin.test.assertEquals

internal class KotlinUtilsTest {
    @Test
    fun `invalid characters replaced with nothing in class name`() {
        assertEquals("", KotlinDefaults.createSafeClassName(";'\"[]{}-+=_)!@#$%^&*()", emptyList()))
    }

    @Test
    fun `class name is upper camel cased with invalid characters replaced`() {
        assertEquals("UpperCamelCaseMe", KotlinDefaults.createSafeClassName("Upper_camel_case_me", emptyList()))
    }

    @Test
    fun `valid class name is untouched`() {
        assertEquals("ValidClassName7", KotlinDefaults.createSafeClassName("ValidClassName7", emptyList()))
    }
}