import net.pwall.json.schema.JSONSchema
import net.pwall.json.schema.codegen.CodeGenerator
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    dependencies {
        classpath("net.pwall.json:json-kotlin-schema:0.31")
        classpath("net.pwall.json:json-kotlin-schema-codegen:0.66")
    }
}

plugins {
    kotlin("jvm") version "1.6.10"
}

group = "com.miya10kei.playground.kotlin.jsonschema"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}


val schemaDirectory = file("src/main/resources/json-schema")
val schemaExtension = "schema.json"
val validateJsonSchemaTask = tasks.register("validateJsonSchema") {
    group = "json schema"
    description = "Validate json schema with test json"

    val testJsonDirectory = file("src/test/resources/test-json")
    val testJsonExtension = "test.json"

    doLast {
        val result = schemaDirectory
            .listFiles { _, name -> name.endsWith(schemaExtension) }
            ?.mapNotNull { schemaFile ->
                val schema = JSONSchema.parseFile(schemaFile.path)
                logger.quiet("[Schema] ${schemaFile.name}")

                testJsonDirectory.resolve(schemaFile.schemaName)
                    .listFiles { _, name -> name.endsWith(testJsonExtension) }
                    ?.map { testJsonFile ->
                        logger.quietL1("[Test] ${testJsonFile.name}")
                        schema.validateBasic(testJsonFile.readText()).also {
                            it.errors?.last()?.run {
                                logger.errorL2("[Error] $instanceLocation: $error")
                            }
                        }
                    }
                    ?.all { it.valid }
            }
            ?.all { it } ?: true
        if (!result) throw TaskExecutionException(this, Exception("Failed to validate json schema"))
    }
}

tasks.register("generateCodeFromJsonSchema") {
    group = "json schema"
    description = "Generate kotlin code from json schema"
    dependsOn(validateJsonSchemaTask)

    val baseDirectory = "src/main/kotlin"
    val basePackage = "${project.group}.payload"

    doLast {
        file(baseDirectory).resolve(basePackage.replace(".", "/"))
            .run { if (!this.exists()) this.mkdirs() }
        schemaDirectory
            .listFiles { _, name -> name.endsWith(schemaExtension) }
            ?.run {
                val generator = CodeGenerator(baseDirectoryName = baseDirectory, basePackageName = basePackage)
                generator.generate(*this)
            }
    }
}

val File.schemaName: String
    get() = name.substringBefore(".")

@Suppress("unused")
fun Logger.quietL1(message: String) =
    logger.quiet("  $message")

@Suppress("unused")
fun Logger.errorL2(message: String) =
    logger.error("    $message")
