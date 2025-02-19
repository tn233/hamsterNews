plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("androidx.room")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.tn233.hamster"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tn233.hamster"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    room {
        schemaDirectory("$projectDir/schemas")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.room.common){
        exclude("com.intellij", "annotations")
    }
    ksp(libs.androidx.room.compiler){
        exclude("com.intellij", "annotations")
    }
    implementation(libs.androidx.room.ktx){
        exclude("com.intellij", "annotations")
    }
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    annotationProcessor(libs.compiler)

    implementation(libs.paging.runtime)
    // optional - Jetpack Compose integration4
    implementation(libs.androidx.paging.compose)

//    implementation(libs)


//    val core_version = "1.13.1"
//
//    // Java language implementation
//    implementation(libs.androidx.core)
//    // Kotlin
//    implementation(libs.androidx.core.ktx.v1131)
//
//    // To use RoleManagerCompat
//    implementation(libs.androidx.core.role)
//
//    // To use the Animator APIs
//    implementation(libs.androidx.core.animation)
//    // To test the Animator APIs
//    androidTestImplementation("androidx.core:core-animation-testing:1.0.0")
}