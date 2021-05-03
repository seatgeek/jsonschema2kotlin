package com.seatgeek.jsonschema2kotlin.writer

import okio.Sink

interface SinkFactory {
    fun newFile(fileName: String): Sink
}
