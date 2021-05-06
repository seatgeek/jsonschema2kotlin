@file:JvmName("Main")

package com.seatgeek.jsonschema2kotlin.app

import com.seatgeek.jsonschema2kotlin.Generator
import com.seatgeek.jsonschema2kotlin.interceptor.DataClassInterceptor
import com.seatgeek.jsonschema2kotlin.interceptor.EnumClassInterceptor
import com.seatgeek.jsonschema2kotlin.interceptor.PropertyInterceptor
import com.seatgeek.jsonschema2kotlin.interceptor.recipes.GsonSerializedNamePropertyInterceptor
import com.seatgeek.jsonschema2kotlin.interceptor.recipes.ParcelizeDataClassInterceptor
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.delimiter
import kotlinx.cli.vararg
import util.listFilesRecursive
import java.io.File

/**
 * Arg parsing: https://github.com/Kotlin/kotlinx-cli
 */
fun main(args: Array<String>) {
    val parser = ArgParser("jsonschema2kotlin")

    val inputArg by parser.argument(
        ArgType.String,
        description = "Input JsonSchema file(s) and director[y/ies]"
    ).vararg()

    val outputArg by parser.option(
        ArgType.String,
        shortName = "o",
        fullName = "output",
        description = "Output directory; emission = STDOUT"
    ).default(Generator.Builder.Config.Output.Stdout.NAME)

    val packageNameArg by parser.option(
        ArgType.String,
        shortName = "p",
        description = "Package name",
        fullName = "package-name"
    ).default("")

    val classNameFormatArg by parser.option(
        ArgType.String,
        shortName = "fd",
        fullName = "data-class-name-format",
        description = "A sprintf format string for data class names, e.g. \"Api%sModel\"",
    )

    val enumNameFormatArg by parser.option(
        ArgType.String,
        shortName = "fe",
        fullName = "enum-class-name-format",
        description = "A sprintf format string for enum class names, e.g. \"Api%sModel\""
    )

    val propertyNameFormatArg by parser.option(
        ArgType.String,
        shortName = "fp",
        fullName = "property-name-format",
        description = "A sprintf format string for property names, e.g. \"apiProp%s\"",
    )

    val recipes by parser.option(
        ArgType.Choice(
            choices = Recipes.values().toList(),
            toVariant = {
                Recipes.valueOf(it)
            },
            variantToString = {
                it.name
            }
        ),
        shortName = "r",
        fullName = "recipes"
    ).delimiter(",")

    parser.parse(args)

    val dataClassNameInterceptors = classNameFormatArg?.let(::StringDataClassNameInterceptors)?.let(::listOf) ?: emptyList()
    val enumClassNameInterceptors = enumNameFormatArg?.let(::StringEnumClassNameInterceptors)?.let(::listOf) ?: emptyList()
    val propertyNameInterceptors = propertyNameFormatArg?.let(::StringPropertyNameInterceptor)?.let(::listOf) ?: emptyList()

    val inFiles = Generator.Builder.Config.Input(
        inputArg.map { File(it) }
            .flatMap { it.listFilesRecursive() }
    )

    val output = when (outputArg) {
        Generator.Builder.Config.Output.Stdout.NAME -> Generator.Builder.Config.Output.Stdout
        else -> File(outputArg).takeIf { it.isDirectory }?.let(Generator.Builder.Config.Output::Directory)
    } ?: throw IllegalArgumentException("Output must be a directory, was '$outputArg'")

    Generator.builder(inFiles, output)
        .updateConfig {
            it.copy(
                packageName = packageNameArg,
                dataClassInterceptors = it.dataClassInterceptors
                    .plus(dataClassNameInterceptors)
                    .plus(recipes.flatMap { it.interceptors.dataClassNameInterceptors }),
                enumClassInterceptors = it.enumClassInterceptors
                    .plus(enumClassNameInterceptors)
                    .plus(recipes.flatMap { it.interceptors.enumClassNameInterceptors }),
                propertyInterceptors = it.propertyInterceptors
                    .plus(propertyNameInterceptors)
                    .plus(recipes.flatMap { it.interceptors.propertyInterceptors })
            )
        }
        .build()
        .generate()
}

internal enum class Recipes(val interceptors: Interceptors) {
    PARCELIZE(Interceptors(dataClassNameInterceptors = listOf(ParcelizeDataClassInterceptor))),
    GSON(Interceptors(propertyInterceptors = listOf(GsonSerializedNamePropertyInterceptor)));

    internal data class Interceptors(
        val dataClassNameInterceptors: List<DataClassInterceptor> = emptyList(),
        val enumClassNameInterceptors: List<EnumClassInterceptor> = emptyList(),
        val propertyInterceptors: List<PropertyInterceptor> = emptyList(),
    )
}