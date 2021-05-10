package com.seatgeek.jsonschema2kotlin.writer.kotlin

import com.seatgeek.jsonschema2kotlin.interceptor.DataClassInterceptor
import com.seatgeek.jsonschema2kotlin.interceptor.EnumClassInterceptor
import com.seatgeek.jsonschema2kotlin.interceptor.PropertyInterceptor
import com.seatgeek.jsonschema2kotlin.writer.kotlin.interceptor.BooleanPropertyNamePrefixInterceptor
import com.seatgeek.jsonschema2kotlin.writer.kotlin.interceptor.CommonCaseEnumClassInterceptor
import com.seatgeek.jsonschema2kotlin.writer.kotlin.interceptor.SafeClassNameInterceptor
import com.seatgeek.jsonschema2kotlin.writer.kotlin.interceptor.SafePropertyNameInterceptor

object KotlinDefaults {
    fun defaultDataClassInterceptors(): List<DataClassInterceptor> = listOf(SafeClassNameInterceptor)

    fun defaultEnumClassInterceptors(): List<EnumClassInterceptor> = listOf(CommonCaseEnumClassInterceptor)

    fun defaultPropertyInterceptors(): List<PropertyInterceptor> = listOf(SafePropertyNameInterceptor, BooleanPropertyNamePrefixInterceptor)
}