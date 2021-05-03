package com.seatgeek.jsonschema2kotlin.writer

import net.jimblackler.jsonschemafriend.Schema

interface SchemaModelWriter {
    fun write(sinkFactory: SinkFactory, schema: Schema)
}