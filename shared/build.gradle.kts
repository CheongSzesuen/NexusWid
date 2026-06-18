import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.koin.compiler)
}

compose.resources {
    publicResClass = true
}

kotlin {
    compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")

    android {
        namespace = "cn.waijade.nexuswid.shared"
        compileSdk = 37
        minSdk = 26

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }

        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project.dependencies.platform(libs.androidx.compose.bom))
            implementation(libs.components.resources)
            implementation(libs.androidx.ui)
            implementation(libs.androidx.ui.graphics)
            implementation(libs.androidx.ui.tooling)
            implementation(libs.androidx.lifecycle.runtime.ktx)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.material3)
            implementation(libs.androidx.material.icons.core)
            implementation(libs.androidx.material.icons.extended)
            implementation(libs.androidx.adaptive)

            implementation(libs.androidx.navigation3.runtime)
            implementation(libs.androidx.navigation3.ui)
            implementation(libs.androidx.compose.adaptive.navigation3)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.room.ktx)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation(libs.coil.compose)
        }

        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.androidx.compose.bom))

            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.lifecycle.runtime.ktx)

            implementation(libs.koin.android)

            implementation(libs.ktor.client.okhttp)
            implementation(libs.coil.network.okhttp)
        }
    }
}

dependencies {
    ksp(libs.androidx.room.compiler)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
