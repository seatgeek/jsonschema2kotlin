
plugins {
    kotlin("jvm")
    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        create("jsonschema2kotlin") {
            id = "com.seatgeek.gradle.jsonschema2kotlin"
            implementationClass = "org.example.GreetingPlugin"
        }
    }
}

group = "com.seatgeek.jsonschem2kotlin"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":jsonschema2kotlin-lib"))
    implementation(kotlin("stdlib"))

    compileOnly(gradleApi())
    implementation(Deps.plugins.kotlin)
    compileOnly(Deps.plugins.android)
}
