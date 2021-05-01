@file:JvmName("Main")
package com.seatgeek.jsonschema2kotlin.app

import com.seatgeek.jsonschema2kotlin.Generator
import com.seatgeek.jsonschema2kotlin.Output
import kotlinx.cli.*
import java.io.File

private data class Options(
    val input: List<File>,
    val output: Output,
    val packageName: String?,
)

/**
 * Arg parsing: https://github.com/Kotlin/kotlinx-cli
 */
fun main(args: Array<String>) {
    val parser = ArgParser("jsonschema2kotlin")

    val input by parser.argument(
        ArgType.String,
        description = "Input JsonSchema file(s) and director[y/ies]"
    ).vararg()

    val output by parser.option(
        ArgType.String,
        shortName = "o",
        description = "Output directory"
    ).default(Output.Stdout.NAME)

    val packageName by parser.option(
        ArgType.String,
        shortName = "p",
        description = "Package name",
        fullName = "package-name"
    ).default("")

    // TODO Conceptually, this project could be more than Kotlin (producing e.g. Markdown), maybe consider format/language
    //   args (and maybe also a project rename...?

    parser.parse(args)

    val inArgs = input.map { File(it) }
        // Only non-null if all args are a file or a directory
        .takeIf { file -> file.all { it.isFile || it.isDirectory } }
        ?: throw IllegalArgumentException("Input files must all be files and directories")

    val outputResult = if (output == Output.Stdout.NAME) {
        Output.Stdout
    } else {
        val outputFile = File(output)
        when {
            outputFile.isDirectory -> Output.Directory(outputFile)
            else -> null
        }
    } ?: throw IllegalArgumentException("Output must be a directory, was '$output'")

    val options = Options(
        input = inArgs,
        output = outputResult,
        packageName = packageName
    )

    build(options)
}

private fun build(options: Options) {
    val generator = Generator.builder(options.input, options.output)
        .apply {
            if (options.packageName?.isNotEmpty() == true) {
                withPackageName(options.packageName)
            }
        }
        .build()

    generator.generate()
}