package com.seatgeek.jsonschema2kotlin.writer.kotlin

import com.seatgeek.jsonschema2kotlin.writer.kotlin.interceptor.CommonCaseEnumClassInterceptor
import com.squareup.kotlinpoet.TypeSpec
import org.junit.Test
import org.mockito.kotlin.mock
import kotlin.test.assertEquals

internal class CommonCaseEnumClassInterceptorTest {
    @Test
    fun `enum cases as all caps snake case`() {
        assertEquals(
            "NOT_VALID_AT_ALL",
            CommonCaseEnumClassInterceptor.intercept(
                mock(),
                TypeSpec.enumBuilder("ExampleEnum")
                    .addEnumConstant("not-valid_at//all")
                    .build()
            ).enumConstants.keys.first()
        )
    }
}