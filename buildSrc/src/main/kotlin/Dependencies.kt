object Versions {
    const val kotlin = "1.5.0"
}

object Deps {
    val plugins = Plugins

    object Plugins {
        const val android = "com.android.tools.build:gradle:4.1.3"
        const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    }
}