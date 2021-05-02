package util

import java.io.File

fun File.listFilesRecursive(): List<File> = listFiles()
    ?.flatMap {
        if (it.isDirectory) {
            it.listFilesRecursive()
        } else {
            listOf(it)
        }
    } ?: throw IllegalStateException("Listing files failed for $this")