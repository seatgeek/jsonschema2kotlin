package com.seatgeek.jsonschema2kotlin.writer.kotlin

import org.junit.Test
import kotlin.test.assertEquals

internal class KotlinUtilsTest {
    @Test
    fun `invalid characters replaced with nothing in class name`() {
        assertEquals("", KotlinUtils.createSafeClassName(";'\"[]{}-+=_)!@#$%^&*()", emptyList()))
    }

    @Test
    fun `class name is upper camel cased with invalid characters replaced`() {
        assertEquals("UpperCamelCaseMe", KotlinUtils.createSafeClassName("Upper_camel_case_me", emptyList()))
    }

    @Test
    fun `valid class name is untouched`() {
        assertEquals("ValidClassName7", KotlinUtils.createSafeClassName("ValidClassName7", emptyList()))
    }
}