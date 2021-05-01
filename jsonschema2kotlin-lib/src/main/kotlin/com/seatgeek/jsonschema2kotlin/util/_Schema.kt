package com.seatgeek.jsonschema2kotlin.util

import net.jimblackler.jsonschemafriend.Schema

enum class SchemaType {
    STRING,
    OBJECT,
    INTEGER,
    ARRAY,
    BOOLEAN,
    NUMBER;

    companion object {
        fun fromString(typeString: String): SchemaType {
            return when (typeString) {
                "string" -> STRING
                "object" -> OBJECT
                "integer" -> INTEGER
                "number" -> NUMBER
                "array" -> ARRAY
                "boolean" -> BOOLEAN
                else -> throw IllegalArgumentException("Unexpected/unhandled type string '$typeString'")
            }
        }
    }
}

val Schema.type: SchemaType
    get() {
        val schemaObject = schemaObject
        return if (schemaObject is Map<*, *>) {
            schemaObject["type"] as? String
        } else {
            null
        }?.let {
            SchemaType.fromString(it)
        } ?: throw IllegalStateException("Trying to parse Schema.type failed for $this")
    }