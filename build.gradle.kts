plugins {
    base
    kotlin("jvm") version "1.4.32" apply false
}

allprojects {
    tasks.withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    repositories {
        jcenter()
    }
}

dependencies {
}