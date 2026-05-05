plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kotlinx.jsPlainObjects)
}

group = "me.user"
version = "1.0-SNAPSHOT"

kotlin {
    js {
        browser()
        useEsModules()
        binaries.executable()

        compilerOptions {
            optIn.add("kotlin.js.ExperimentalJsExport")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jsMain.dependencies {
            implementation(libs.kotlinx.browser)
            implementation(npm("pretty-print-json", "3.0.5"))
        }
    }
}