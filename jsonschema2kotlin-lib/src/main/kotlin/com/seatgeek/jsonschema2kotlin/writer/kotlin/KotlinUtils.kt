package com.seatgeek.jsonschema2kotlin.writer.kotlin

object KotlinUtils {
    fun createSafeClassName(title: String, prefix: String = "", suffix: String = ""): String {
        val cleanName = when {
            // Name is fine as-is
            // I _think_ we shouldn't have to worry about reserved keywords given all reserved
            // keywords are lowercase and these will always be UpperCamelCase
            title.matches(Regex("^[A-Z][A-z0-9]+$")) -> "$title"

            // Name needs cleansing
            else -> {
                // Remove bad characters
                title.replace(Regex("[^A-z0-9]+"), ";").let {
                    it.toCharArray().mapIndexed { index, c ->
                        // Uppercase everything after replaced bad chars for UpperCamelCase
                        if (index == 0 || it[index - 1] == ';') {
                            c.toUpperCase()
                        } else {
                            c
                        }
                    }
                }.toString().replace(";", "")
            }
        }

        return "$prefix$cleanName$suffix"
    }
}