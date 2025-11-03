
plugins {
    id("java")
    id("com.gradleup.shadow") version "9.2.2"
}

val git : String = versionBanner()
val builder : String = builder()
ext["git_version"] = git
ext["builder"] = builder

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
                Pair("project_version", rootProject.properties["project_version"]!!),
                Pair("config_version", rootProject.properties["config_version"]!!)
            )
        }
    }
}

fun versionBanner(): String {
    return try {
        val result = providers.exec {
            commandLine("git", "rev-parse", "--short=8", "HEAD")
        }.standardOutput.asText.get().trim()
        result
    } catch (e: Exception) {
        "Unknown"
    }
}

fun builder(): String {
    return try {
        val result = providers.exec {
            commandLine("git", "config", "user.name")
        }.standardOutput.asText.get().trim()
        result
    } catch (e: Exception) {
        "Unknown"
    }
}
