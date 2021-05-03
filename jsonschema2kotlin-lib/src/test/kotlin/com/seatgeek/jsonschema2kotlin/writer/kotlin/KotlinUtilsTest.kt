package com.seatgeek.jsonschema2kotlin.writer.kotlin

import org.junit.Test

internal class KotlinUtilsTest {
    val basicJsonSchemaValidName = """
{
  "$\id": "https://example.com/person.schema.json",
  "$\schema": "https://json-schema.org/draft/2020-12/schema",
  "title": "Person",
  "type": "object",
  "properties": {
    "firstName": {
      "type": "string",
      "description": "The person's first name."
    },
    "lastName": {
      "type": "string",
      "description": "The person's last name."
    },
    "age": {
      "description": "Age in years which must be equal to or greater than zero.",
      "type": "integer",
      "minimum": 0
    }
  }
}
    """.trimIndent()

    @Test
    fun `invalid characters replaced with nothing in class name`() {
        // assertEquals("", Safe)
    }

    @Test
    fun `class name is upper camel cased with invalid characters replaced`() {
        // assertEquals("UpperCamelCaseMe", KotlinDefaults.createSafeClassName("Upper_camel_case_me", emptyList()))
    }

    @Test
    fun `valid class name is untouched`() {
        // assertEquals("ValidClassName7", KotlinDefaults.createSafeClassName("ValidClassName7", emptyList()))
    }
}