pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public")
        maven("https://jitpack.io")
    }
}

rootProject.name = "Zalith Launcher"
include(":jre_lwjgl3glfw")
include(":ZalithLauncher")
include(":minecraft-plugin")
