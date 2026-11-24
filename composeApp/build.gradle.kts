import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        mainRun {
            mainClass.set("com.mindsparq.os.MainKt")
        }
    }

    sourceSets {
        val desktopMain by getting

        commonMain {
            kotlin.srcDir(layout.buildDirectory.dir("generated/supabase/kotlin"))
            dependencies {
                implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.materialIconsExtended)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            implementation(libs.supabase.auth)
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.storage)
            }
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.cio)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.mindsparq.os"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mindsparq.os"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

compose.desktop {
    application {
        mainClass = "com.mindsparq.os.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.AppImage)
            packageName = "MindSparQ OS"
            packageVersion = "1.0.0"
            description = "MindSparQ Education & Technology ERP"
            vendor = "MindSparQ Education & Technology"
        }
    }
}

val generateSupabaseConfig by tasks.registering {
    val envFile = rootProject.file(".env")
    val outDir = layout.buildDirectory.dir("generated/supabase/kotlin")
    val props = Properties().apply {
        if (envFile.exists()) {
            envFile.inputStream().use { load(it) }
        }
    }
    val resolvedUrl = (findProperty("MINDSPARQ_SUPABASE_URL") as? String)
        ?: props.getProperty("MINDSPARQ_SUPABASE_URL")
        ?: System.getenv("MINDSPARQ_SUPABASE_URL")
        ?: ""
    val resolvedAnonKey = (findProperty("MINDSPARQ_SUPABASE_ANON_KEY") as? String)
        ?: props.getProperty("MINDSPARQ_SUPABASE_ANON_KEY")
        ?: System.getenv("MINDSPARQ_SUPABASE_ANON_KEY")
        ?: ""

    outputs.dir(outDir)
    doLast {
        val target = outDir.get().asFile.resolve("com/mindsparq/os/core/network/GeneratedSupabaseConfig.kt")
        target.parentFile.mkdirs()
        target.writeText(
            """
            package com.mindsparq.os.core.network

            internal object GeneratedSupabaseConfig {
                const val URL: String = "${resolvedUrl.replace("\"", "\\\"")}"
                const val ANON_KEY: String = "${resolvedAnonKey.replace("\"", "\\\"")}"
            }
            """.trimIndent() + "\n"
        )
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    dependsOn(generateSupabaseConfig)
}

