# jsonschema2kotlin-lib

This is the core library that implements conversion from JsonSchema into Kotlin data class
implementations.

## Dependency

```groovy
repositories {
    maven { url "https://jitpack.io" }
}
dependencies {
     implementation 'com.github.seatgeek:jsonschema2kotlin:0.1'
}
```

## Usage

```kotlin
val input = Config.Input(listOf(File("SchemaFile1.json"), File("SchemaFile2.json")))
val output = Config.Output.Directory(File("outputs/"))

Generator.builder(input, output)
    .build()
    .generate()
```

### Interceptors

You can customize the output of the generated code by adding various interceptors to the `Generator` when building.

```kotlin
Interceptor
```

## Dependencies & acknowledgements

- kotlinpoet
- jsonschemafriend

This library leans heavily on https://github.com/jimblackler/jsonschemafriend for parsing the schema
into models that can then be used to write out Kotlin data classes