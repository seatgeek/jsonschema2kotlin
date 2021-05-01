package com.seatgeek.jsonschema2kotlin.writer

import okio.Sink
import okio.sink
import java.io.File
import java.io.FileOutputStream

fun interface SinkFactory {
    fun newFile(fileName: String): Sink

    companion object {
        fun stdout(): SinkFactory = object : SinkFactory {
            val sink = System.out.sink()

            override fun newFile(fileName: String): Sink = sink
        }

        fun files(rootDirectory: File): SinkFactory = rootDirectory.takeIf { it.isDirectory }
            ?.absolutePath
            ?.let { rootPath ->
                return SinkFactory {
                    FileOutputStream(rootPath + File.separator + it).sink()
                }
            } ?: throw IllegalStateException("$rootDirectory isn't a directory")
    }
}
