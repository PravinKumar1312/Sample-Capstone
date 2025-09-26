// settings.gradle.kts

pluginManagement {
    repositories {
        gradlePluginPortal()
        google() // This is now a regular, unrestricted repository
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SkillSync AI"
include(":app")