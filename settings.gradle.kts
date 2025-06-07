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
    // Jos et halua käyttää FAIL_ON_PROJECT_REPOS, voit kommentoida tai poistaa seuraavan rivin:
    // repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
    }

    // versionCatalogs {
    // create("libs") {
    //  from(files("gradle/libs.versions.toml"))
     //   }
   // }
}

rootProject.name = "maksut"
include(":app")
