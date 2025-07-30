
plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

group = "dev.secondsun"
version = "unspecified"

repositories {
    mavenCentral()
}

kotlin {
    jvm("desktop")

    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.junit.jupiter.api)
                implementation(libs.junit.jupiter.engine)
            }
        }


    }
}


tasks.withType<Test> {
    useJUnitPlatform()
}

