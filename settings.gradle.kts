pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie" }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.11"
}

// MC_ONLY=1.21.11,1.21.4 ile sadece istenen sürümler yapılandırılır (hızlı derleme)
val allVersions = listOf("1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5",
    "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11")
val vcs = "1.21.1"
val only = System.getenv("MC_ONLY")?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
val selected = if (only == null) allVersions else allVersions.filter { it in only || it == vcs }

stonecutter {
    create(rootProject) {
        versions(*selected.toTypedArray())
        vcsVersion = vcs
    }
}

rootProject.name = "windchargecounter"
