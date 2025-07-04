pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Track Mate"
include(":app")
include(":core:testing")
include(":core:data:database")
include(":core:network")
include(":feature:home")
include(":core:ui-common")
include(":core:design")
include(":feature:history")
include(":feature:analytics")
include(":core:domain")
include(":core:data:data-entry")
