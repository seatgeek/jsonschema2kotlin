plugins {
    base
    kotlin("jvm") version "1.4.32" apply false
}

allprojects {
    group = "com.seatgeek.jsonschem2kotlin"
    version = "0.1-SNAPSHOT"

    tasks.withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    repositories {
        jcenter()
    }
}

dependencies {
    subprojects.forEach {
        archives(it)
    }
}