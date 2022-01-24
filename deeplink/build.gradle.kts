@file:Suppress("PropertyName")

plugins {
    id("com.android.library")
    kotlin("android")
    id("de.mobilej.unmock")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish.base")
}

android {
    compileSdk = 30
    defaultConfig {
        minSdk = 17
        targetSdk = 30
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(libs.kotlinStdlib)
    implementation(libs.okio)

    unmock(libs.robolectricAndroid)
    testImplementation(libs.kotlinTestJunit)
}

unMock {
    keep("android.net.Uri")
    keepStartingWith("libcore.")
    keepAndRename("java.nio.charset.Charsets").to("xjava.nio.charset.Charsets")
}

mavenPublishing {
    configure(
        com.vanniktech.maven.publish.AndroidLibrary(
            com.vanniktech.maven.publish.JavadocJar.Dokka("dokkaJavadoc")
        )
    )
}
