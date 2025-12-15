// settings.gradle.kts DOSYASI

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) // Bu satır hatanın nedenini açıklar
    repositories {
        google()
        mavenCentral()
        // Gerekirse başka depolar, örn: jcenter() veya maven { url '...' }
    }
}

rootProject.name = "HaritaP"
include(":app")
