package jsonschema2kotlin

import com.android.builder.model.v2.models.AndroidProject
import com.seatgeek.jsonschema2kotlin.Generator
import com.seatgeek.jsonschema2kotlin.Generator.Builder.Config
import com.seatgeek.jsonschema2kotlin.interceptor.DataClassInterceptor
import com.seatgeek.jsonschema2kotlin.interceptor.EnumClassInterceptor
import com.seatgeek.jsonschema2kotlin.interceptor.PropertyInterceptor
import gradle.kotlin.OutputTask
import gradle.kotlin.sources
import jsonschema2kotlin.JsonSchema2KotlinPlugin.Extension.Companion.applyTo
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * A plugin for running the [jsonschema2kotlin library](https://github.com/seatgeek/jsonschema2kotlin) to run in the given project/module,
 * generating Kotlin data classes for the given [JsonSchema](https://json-schema.org/) files
 *
 * TODO move this out of here and into the linked project
 */
class JsonSchema2KotlinPlugin : Plugin<Project> {

    private val android = AtomicBoolean(false)
    private val kotlin = AtomicBoolean(false)

    override fun apply(project: Project) {
        project.logger.lifecycle("Applying JsonSchema2KotlinPlugin to $project")

        val androidPluginHandler = { _: Plugin<*> -> android.set(true) }
        project.plugins.withId("com.android.application", androidPluginHandler)
        project.plugins.withId("com.android.library", androidPluginHandler)
        project.plugins.withId("com.android.instantapp", androidPluginHandler)
        project.plugins.withId("com.android.feature", androidPluginHandler)
        project.plugins.withId("com.android.dynamic-feature", androidPluginHandler)

        val kotlinPluginHandler = { _: Plugin<*> -> kotlin.set(true) }
        project.plugins.withId("org.jetbrains.kotlin.multiplatform", kotlinPluginHandler)
        project.plugins.withId("org.jetbrains.kotlin.android", kotlinPluginHandler)
        project.plugins.withId("org.jetbrains.kotlin.jvm", kotlinPluginHandler)
        project.plugins.withId("kotlin2js", kotlinPluginHandler)

        val extension = project.extensions.create(JS2K_EXT, Extension::class.java)
        val outputDir = File(project.buildDir, "${AndroidProject.FD_GENERATED}/jsonshema2kotlin/code/".replace('/', File.separatorChar))

        project.afterEvaluate { afterEvaluateProject ->
            afterEvaluateProject.setupTasks(extension, outputDir)
        }
    }

    private fun Project.setupTasks(extension: Extension, outputDir: File) {
        check(kotlin.get()) { "JsonSchema2Kotlin Gradle plugin applied in project '${project.path}' but no supported Kotlin plugin was found" }

        sources().forEach { source ->
            val generate = tasks.register("generate${source.name.capitalize()}JsonSchemaDataClasses", GenerateTask::class.java) {
                it.extension = extension
                it.outputDir = File(outputDir, source.name)
            }

            source.sourceDirectorySet.srcDir(File(outputDir, source.name))
            source.registerTaskDependency(generate as TaskProvider<OutputTask>)

            tasks.matching { it.name.matches(Regex(".*generate.*Sources.*")) }
                .configureEach { task ->
                    task.dependsOn(generate.get().generate())
                }
        }
    }

    open class GenerateTask @Inject constructor() : DefaultTask(), OutputTask {
        lateinit var extension: Extension

        @get:OutputDirectory
        @get:Optional
        override val outputDirectory: File by lazy {
            outputDir.apply {
                deleteRecursively()
                mkdirs()
            }
        }

        lateinit var outputDir: File

        @TaskAction
        fun generate() {
            val input = Config.Input(extension.inputs.orNull ?: throw IllegalArgumentException("`input` (file(s)/directory/directories) not provided"))
            val output = Config.Output.Directory(outputDirectory)

            logger.lifecycle("Generating Kotlin data classes for '$input' into '$output'")

            Generator.builder(input, output)
                .updateConfig { config -> extension.applyTo(config) }
                .build()
                .generate()
        }
    }

    abstract class Extension {
        @get:InputFiles
        abstract val inputs: ListProperty<File>

        @get:Input
        abstract val packageName: Property<String>

        @get:Input
        abstract val parcelize: Property<Boolean>

        @get:Input
        @get:Optional
        abstract val dataClassInterceptors: ListProperty<DataClassInterceptor?>

        @get:Input
        @get:Optional
        abstract val enumClassInterceptors: ListProperty<EnumClassInterceptor?>

        @get:Input
        @get:Optional
        abstract val propertyInterceptors: ListProperty<PropertyInterceptor?>

        companion object {
            fun Extension.applyTo(config: Config): Config {
                return config.copy(
                    packageName = packageName.getOrElse(config.packageName),
                    parcelize = parcelize.getOrElse(config.parcelize),
                    dataClassInterceptors = dataClassInterceptors.orNull?.filterNotNull()?.takeIf { it.isNotEmpty() } ?: config.dataClassInterceptors,
                    enumClassInterceptors = enumClassInterceptors.orNull?.filterNotNull()?.takeIf { it.isNotEmpty() } ?: config.enumClassInterceptors,
                    propertyInterceptors = propertyInterceptors.orNull?.filterNotNull()?.takeIf { it.isNotEmpty() } ?: config.propertyInterceptors
                )
            }
        }

        override fun toString(): String =
            "Extension(inputs=${inputs.orNull}, packageName=${packageName.orNull}, parcelize=${parcelize.orNull})"
    }

    companion object {
        const val JS2K_EXT = "jsonSchema2Kotlin"
    }
}
