# jsonschema2kotlin-lib

This is the core library that implements conversion from JsonSchema into Kotlin data class
implementations.

## Dependencies & acknowledgements

- kotlinpoet
- jsonschemafriend

This library leans heavily on https://github.com/jimblackler/jsonschemafriend for parsing the schema
into models that can then be used to write out Kotlin data classes