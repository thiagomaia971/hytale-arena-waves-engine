import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.compileOnly

plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.3.1"
    id("run-hytale")
    kotlin("jvm")
}

group = findProperty("pluginGroup") as String? ?: "com.miilhozinho"
var pluginName = findProperty("pluginName") as String? ?: "PluginName"
version = findProperty("pluginVersion") as String? ?: "1.0.0"
description = findProperty("pluginDescription") as String? ?: "A Hytale plugin template"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    // Hytale Server API (provided by server at runtime)
    compileOnly(files("libs/HytaleServer.jar"))
    compileOnly(files("${System.getenv("APPDATA")}/Hytale/install/release/package/game/latest/Server/HytaleServer.jar"))
    compileOnly(files("E:\\Worksplace\\hytale-arena-waves-engine\\src\\main\\dependencies\\MultipleHUD-1.0.3.jar"))
    compileOnly(files("E:\\Worksplace\\hytale-arena-waves-engine\\src\\main\\dependencies\\HyUI-0.5.0-all.jar"))

    // Common dependencies (will be bundled in JAR)
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains:annotations:24.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(kotlin("stdlib-jdk8"))
}

// Configure server testing
runHytale {
    // TODO: Update this URL when Hytale server is available
    // Using Paper server as placeholder for testing the runServer functionality
//    jarUrl = "https://fill-data.papermc.io/v1/objects/d5f47f6393aa647759f101f02231fa8200e5bccd36081a3ee8b6a5fd96739057/paper-1.21.10-115.jar"
    jarUrl = "file:///C://Users/thiag/AppData/Roaming/Hytale/install/release/package/game/latest/Server/HytaleServer.jar"
    assetsPath = "file:///C://Users/thiag/AppData/Roaming/Hytale/install/release/package/game/latest/Assets.zip"
}


sourceSets {
    main {
        resources {
            srcDir("assets")
        }
    }
}

tasks {
    // Configure Java compilation
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release = 25
    }


    // Configure resource processing
    processResources {
        filteringCharset = Charsets.UTF_8.name()
        
        // Replace placeholders in manifest.json
        val props = mapOf(
            "Group" to project.group,
            "Name" to pluginName,
            "Version" to project.version,
            "Description" to project.description
        )
        inputs.properties(props)
        
        filesMatching("manifest.json") {
            expand(props)
        }
    }
    
    // Configure ShadowJar (bundle dependencies)
    shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("")
        
        // Relocate dependencies to avoid conflicts
        relocate("com.google.gson", "com.yourplugin.libs.gson")
        
        // Minimize JAR size (removes unused classes)
        minimize()
        doLast {
            copy {
                from(archiveFile)
                into("run/mods")
            }
            copy {
                from("main/builtin/mdevtools-1.0.4.jar")
                into("builtin")
            }
        }
    }

    // Configure tests
    test {
        useJUnitPlatform()
    }
    
    // Make build depend on shadowJar
    build {
        dependsOn(shadowJar)
    }
}

// Configure Java toolchain
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}
