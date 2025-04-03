plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.svcp.steptracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.svcp.steptracker"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        multiDexEnabled = true

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
}

dependencies {
    // Core Android dependencies
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.activity)
    implementation (libs.gridlayout)

    // multidex support
    implementation(libs.multidex)

    // Firebase dependencies - use Firebase BOM to manage versions
    implementation(platform(libs.firebase.bom))
    implementation(libs.com.google.firebase.firebase.auth)
    implementation(libs.com.google.firebase.firebase.firestore)
    implementation(libs.firebase.analytics)

    // Authentication and credentials
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)

    // Step counter dependencies
    implementation(libs.play.services.fitness)
    implementation(libs.play.services.auth)
    implementation(libs.swiperefreshlayout)
    implementation(libs.firebase.storage)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // dependency for Preferences
    implementation(libs.preference)

    // Glide for image loading
    implementation(libs.glide)
    annotationProcessor (libs.compiler)
}