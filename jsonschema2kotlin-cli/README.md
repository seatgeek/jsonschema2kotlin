# jsonschema2kotlin-cli

This project implements a CLI wrapper around jsonschema2kotlin-lib to make it possible to convert JsonSchema files to Kotlin data classes from the command line.

## Usage

Note that file references (output and input) are relative to this directory, though the commands below should be run from the root project directory

```shell
Usage: jsonschema2kotlin options_list
Arguments: 
    inputArg -> Input JsonSchema file(s) and director[y/ies] { String }
Options: 
    --output, -o [STDOUT] -> Output directory; emission = STDOUT { String }
    --package-name, -p [] -> Package name { String }
    --data-class-name-format, -fd -> A sprintf format string for data class names, e.g. "Api%sModel" { String }
    --enum-class-name-format, -fe -> A sprintf format string for enum class names, e.g. "Api%sModel" { String }
    --property-name-format, -fp -> A sprintf format string for property names, e.g. "apiProp%s" { String }
    --recipes, -r { Value should be one of [PARCELIZE] }
    --help, -h -> Usage info 
```

A command which formats data classes as `Sg[Name]ApiModel`, enum classes as `Sg[Name]Enum`, properties on data classes as `psh[name]`, with a package of
`com.seatgeek.api.model`, pulling JsonSchema files from `../inputs/` (relative to -cli module directory) and outputs to `../outputs/` (again, relative):

```shell
./gradlew run --args="-fd \"Sg%sApiModel\" -fe \"Sg%sEnum\" -fp \"psh%s\" -p com.seatgeek.api.model -o ../outputs/ ../inputs/"
```

## Dependencies & acknowledgements

- kotlinx-cli