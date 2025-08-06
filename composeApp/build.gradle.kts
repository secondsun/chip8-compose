import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.gradle.kotlin.dsl.api
import org.gradle.kotlin.dsl.implementation
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

val monacoConsumer = configurations.create("monacoConsumer") {
    isCanBeConsumed = false
    isCanBeResolved = true
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

dependencies {
    monacoConsumer(project(":monaco", configuration = "monacoBuilder"))
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    
    jvm("desktop")
    

    sourceSets {
        val desktopMain by getting
        
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.compose.webview.multiplatform)
            implementation(compose.materialIconsExtended)
            implementation(compose.materialIconsExtended)
            implementation(libs.filekit.dialogs)
            implementation(libs.filekit.dialogs.compose)
            implementation(libs.compose.components.splitpane)
            implementation(project(":material-color-utils"))
            implementation(project(":chip8"))

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)

            implementation(project(":material-color-utils"))

            // jSystemThemeDetector dependencies
            implementation(libs.slf4j.api)
            implementation(libs.jna.jpms)
            implementation(libs.jna.platform.jpms)
            //This should be a version catalog, but is broken for some reason.
            implementation ("de.jangassen:jfa:1.2.0") {exclude(group = "net.java.dev.jna", module = "jna")}
            implementation(libs.oshi.core)
            implementation(libs.versioncompare)
            implementation(libs.jetbrains.annotations)


        }
    }
}

android {
    namespace = "dev.secondsun.chip8.compose"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "dev.secondsun.chip8.compose"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {

        jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
        jvmArgs("--add-opens", "java.desktop/java.awt.peer=ALL-UNNAMED") // recommended but not necessary

        if (System.getProperty("os.name").contains("Mac")) {
            jvmArgs("--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED")
        }


        mainClass = "dev.secondsun.chip8.compose.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "dev.secondsun.chip8.compose"
            packageVersion = "1.0.0"
        }
    }
}


tasks.register<Copy>("copyMonacoZip") {
    from(configurations.named("monacoConsumer"))
    into(layout.projectDirectory.dir("src/commonMain/composeResources/files"))
}

tasks.named("copyNonXmlValueResourcesForCommonMain").dependsOn("copyMonacoZip")