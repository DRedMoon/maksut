plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    // alias(libs.plugins.kotlin.compose)  ← Poistettu, koska ei enää käytetä Composea
}

android {
    namespace = "com.oma.maksut"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.oma.maksut"
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

    // Poistettu buildFeatures { compose = true }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    // Lisätty AppCompat ja Material-riippuvuudet
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    
    // JSON and Preferences
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.preference:preference-ktx:1.2.1")


    // Poistetaan kaikki Compose-riippuvuudet
    // implementation(libs.androidx.activity.compose)
    // implementation(platform(libs.androidx.compose.bom))
    // implementation(libs.androidx.ui)
    // implementation(libs.androidx.ui.graphics)
    // implementation(libs.androidx.ui.tooling.preview)
    // debugImplementation(libs.androidx.ui.tooling)
    // debugImplementation(libs.androidx.ui.test.manifest)
    // androidTestImplementation(platform(libs.androidx.compose.bom))
    // androidTestImplementation(libs.androidx.ui.test.junit4)

    // Testikehykset:
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
