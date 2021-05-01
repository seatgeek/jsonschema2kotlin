package util

import java.io.File

fun File.listFilesRecursive(): List<File> {
    return listFiles()
        .flatMap {
            if (it.isDirectory) {
                it.listFilesRecursive()
            } else {
                listOf(it)
            }
        }
}