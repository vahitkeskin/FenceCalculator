import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hiltAndroid)
    // KAPT yerine KSP kullanıyoruz
    alias(libs.plugins.ksp)
    kotlin("plugin.serialization") version "2.0.21"
}

android {
    namespace = "com.vahitkeskin.fencecalculator"
    compileSdk = 35 // Genellikle compileSdk en güncel stabil (35) tutulur, 36 preview olabilir.

    defaultConfig {
        applicationId = "com.vahitkeskin.fencecalculator"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // local.properties dosyasından reklam ID'lerini oku
        val properties = Properties()
        val propertiesFile = project.rootProject.file("local.properties")
        if (propertiesFile.exists()) {
            properties.load(propertiesFile.inputStream())
        }

        val adAppId = properties.getProperty("admob.app.id") ?: ""
        val adBannerId = properties.getProperty("admob.banner.id") ?: ""
        val adInterstitialId = properties.getProperty("admob.interstitial.id") ?: ""

        manifestPlaceholders["admobAppId"] = adAppId
        buildConfigField("String", "ADMOB_BANNER_ID", "\"$adBannerId\"")
        buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"$adInterstitialId\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    // Hilt ve Modern Android için Java 17 şart
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation) {
        version {
            strictly(libs.versions.composeFoundation.get())
        }
    }

    // TOML'da tanımladığımız extended icons
    implementation(libs.androidx.compose.material.icons.extended)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Hilt & Navigation
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // ÖNEMLİ: kapt yerine ksp kullanıyoruz
    ksp(libs.hilt.compiler)

    // ML Kit Barcode Scanning
    implementation(libs.mlkit.barcode)

    // QR Generation (ZXing)
    implementation(libs.zxing.core)

    // Phone Number Utility
    implementation(libs.libphonenumber.android)
    implementation(libs.play.services.ads)
}