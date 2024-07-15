pluginManagement {
    repositories {
        google()
        mavenCentral()
        jcenter()
        gradlePluginPortal()
        maven { url = uri("https://www.jitpack.io") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io") }
        maven {
            url = uri("https://maven.google.com")
        }
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
        jcenter() // Warning: this repository is going to shut down soon
    }
}

rootProject.name = "ChildApp"
include(":app")
 