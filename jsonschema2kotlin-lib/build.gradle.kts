import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

group = "com.seatgeek.jsonschema2kotlin"
version = "0.1-SNAPSHOT"

repositories {
    maven("https://jitpack.io")
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    // Used for JsonSchema parsing
    // implementation("net.jimblackler.jsonschemafriend:core:0.10.5")
    implementation("com.github.jimblackler.jsonschemafriend:core:0.10.5")

    // API since TypeSpec, ParameterSpec, PropertySpec are shared in Interceptor implementations

    // TODO update once https://github.com/square/kotlinpoet/pull/1075 merges
    // api("com.squareup:kotlinpoet:1.8.0")
    api(files("libs/kotlinpoet-1.9.0-SNAPSHOT-eff194b.jar"))

    implementation("com.squareup.okio:okio:3.0.0-alpha.4")


    testImplementation(kotlin("test-junit"))
    testImplementation("org.mockito.kotlin:mockito-kotlin:3.1.0")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

val compileKotlin: KotlinCompile by tasks