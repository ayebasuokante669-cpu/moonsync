plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("plugin.serialization") version "1.9.22"
}

android {
    namespace = "com.example.moonsyncapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.moonsyncapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")

    implementation("com.airbnb.android:lottie-compose:6.1.0")

    // Compose BOM (Bill of Materials) - manages all Compose versions
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Compose Foundation (for HorizontalPager)
    implementation("androidx.compose.foundation:foundation")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // AppCompat (for XML themes)
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.foundation)

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Add this line for Material Icons
    implementation("androidx.compose.material:material-icons-extended:1.6.0")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // Swipe actions for notifications
    implementation("me.saket.swipe:swipe:1.2.0")

    // DataStore for theme preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Coil for image loading (profile photo)
    implementation("io.coil-kt:coil-compose:2.5.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // =============================================
    // NEW: Widget dependencies (Jetpack Glance)
    // =============================================

    // Glance for app widgets
    implementation("androidx.glance:glance-appwidget:1.1.0")

    // Glance Material 3 theming support
    implementation("androidx.glance:glance-material3:1.1.0")

    // WorkManager for background widget refresh
    implementation("androidx.work:work-runtime-ktx:2.9.1")
}