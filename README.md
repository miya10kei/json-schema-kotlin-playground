json-schema-kotlin-playground
---
🎳 This repository is a playground where I tried out json schema in kotlin.

# Rule

You must follow the following directory structure.

```
src
├── main
│  └── resources
│     └── json-schema
│        └── ${schemaName}.schema.json
└── test
   └── resources
      └── test-json
         └── ${schemaName}
            └── ${schemaName}.xxx.test.json
```

# Gradle tasks

## validateJsonSchema

Validate json schema with test json file.

```shell
./gradlew validateJsonSchema
```

## generateCodeFromJsonSchema

Generate kotlin code From json schema. This task depends on validateJsonSchema task.

```shell
./gradlew generateCodeFromJsonSchema
```