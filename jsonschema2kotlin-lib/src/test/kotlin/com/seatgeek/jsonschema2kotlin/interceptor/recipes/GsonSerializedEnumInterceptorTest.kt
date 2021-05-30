package com.seatgeek.jsonschema2kotlin.interceptor.recipes

import com.squareup.kotlinpoet.TypeSpec
import org.junit.Test
import org.mockito.kotlin.mock
import kotlin.test.assertEquals

class GsonSerializedEnumInterceptorTest {
    @Test
    fun `enum constants have @SerializedName`() {
        val constantTypeSpecPair = "SHEESH" to TypeSpec.anonymousClassBuilder().build()

        val before = TypeSpec.enumBuilder("MyEnum")
            .addEnumConstant(constantTypeSpecPair.first, constantTypeSpecPair.second)
            .build()

        assertEquals("public enum class MyEnum {\n  SHEESH,\n}\n", before.toString())

        val after = TypeSpec.enumBuilder("MyEnum")
            .apply {
                val (constant, typeSpec) = GsonSerializedEnumInterceptor.intercept(mock(), "sheesh", constantTypeSpecPair)
                addEnumConstant(constant, typeSpec)
            }
            .build()

        assertEquals("public enum class MyEnum {\n  @com.google.gson.annotations.SerializedName(\"sheesh\")\n  SHEESH,\n}\n", after.toString())
    }
}