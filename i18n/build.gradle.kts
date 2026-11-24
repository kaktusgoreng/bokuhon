import mihon.gradle.tasks.GenerateLocalesConfigTask
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(mihonx.plugins.kotlin.multiplatform)
    alias(mihonx.plugins.spotless)

    alias(libs.plugins.moko.resources)
}

kotlin {
    android {
        namespace = "tachiyomi.i18n"

        // TODO(antsy): Remove when https://youtrack.jetbrains.com/issue/KT-83319 is resolved
        withHostTest { }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    dependencies {
        api(libs.moko.resources)
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

// The app drops every locale outside this set from resources.arsc, so listing them here would
// offer languages in the picker that can only fall back to English.
val shippedLocaleDirs = buildSet {
    add("base") // English is the default resources.
    addAll(providers.gradleProperty("locales").get().split(","))
}
// Resolved here rather than with a filtered file tree: a filter spec holding a build script
// reference can't be stored in the configuration cache.
val shippedLocaleFiles = layout.projectDirectory
    .dir("src/commonMain/moko-resources")
    .asFileTree
    .matching { include("**/strings.xml", "**/plurals.xml") }
    .filterTo(mutableListOf()) { it.parentFile.name in shippedLocaleDirs }

androidComponents {
    onVariants { variant ->
        val resSource = variant.sources.res ?: return@onVariants

        val variantName = variant.name.replaceFirstChar { it.uppercase() }
        val task = tasks.register<GenerateLocalesConfigTask>("generate${variantName}LocalesConfig") {
            localeFiles.from(shippedLocaleFiles)
        }
        resSource.addGeneratedSourceDirectory(task) { it.outputDir }
    }
}

multiplatformResources {
    resourcesPackage.set("tachiyomi.i18n")
}
