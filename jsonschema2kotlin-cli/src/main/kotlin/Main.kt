@file:JvmName("Main")

package com.seatgeek.jsonschema2kotlin.app

import com.seatgeek.jsonschema2kotlin.Generator
import com.seatgeek.jsonschema2kotlin.Output
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
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
        description = "Output directory"
    ).default(Output.Stdout.NAME)

    val packageNameArg by parser.option(
        ArgType.String,
        shortName = "p",
        description = "Package name",
        fullName = "package-name"
    ).default("")

    val classNameFormatArg by parser.option(
        ArgType.String,
        shortName = "fc",
        description = "A sprintf format string for class names, e.g. \"Api%sModel\"",
    )

    val propertyNameFormatArg by parser.option(
        ArgType.String,
        shortName = "fp",
        description = "A sprintf format string for property names, e.g. \"apiProp%s\"",
    )

    parser.parse(args)

    val classNameInterceptors = classNameFormatArg?.let(::StringClassNameInterceptors)?.let(::listOf) ?: emptyList()
    val propertyNameInterceptors = propertyNameFormatArg?.let(::StringPropertyNameInterceptor)?.let(::listOf) ?: emptyList()

    val inFiles = inputArg.map { File(it) }
        .flatMap { it.listFilesRecursive() }

    val output = when (outputArg) {
        Output.Stdout.NAME -> Output.Stdout
        else -> File(outputArg).takeIf { it.isDirectory }?.let(Output::Directory)
    } ?: throw IllegalArgumentException("Output must be a directory, was '$outputArg'")

    Generator.builder(inFiles, output)
        .updateConfig {
            it.copy(
                packageName = packageNameArg,
                classInterceptors = it.classInterceptors.plus(classNameInterceptors),
                propertyInterceptors = it.propertyInterceptors.plus(propertyNameInterceptors)
            )
        }
        .build()
        .generate()
}