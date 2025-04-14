import org.gradle.process.internal.ExecException
import java.io.ByteArrayOutputStream

plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta11"
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "com.gradleup.shadow")

    tasks.processResources {
        filteringCharset = "UTF-8"

        filesMatching(arrayListOf("custom-crops.properties")) {
            expand(rootProject.properties)
        }

        filesMatching(arrayListOf("*.yml", "*/*.yml")) {
            expand(
                Pair("project_version", rootProject.properties["project_version"]),
                Pair("config_version", rootProject.properties["config_version"])
            )
        }
    }
}
