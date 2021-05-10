import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
}

group = "com.seatgeek.jsonschema2kotlin.app"
version = "0.1-SNAPSHOT"

application {
    mainClass.set("com.seatgeek.jsonschema2kotlin.app.Main")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("net.jimblackler.jsonschemafriend:core:0.10.5")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.2")
    implementation(project(":jsonschema2kotlin-lib"))

    testImplementation(kotlin("test-junit"))
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Jar> {
    // Otherwise you'll get a "No main manifest attribute" error
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}
