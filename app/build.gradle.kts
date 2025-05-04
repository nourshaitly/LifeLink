// App-level build.gradle.kts
plugins {
    alias(libs.plugins.android.application) // Android plugin
    id("com.google.gms.google-services")    // Apply Firebase plugin
}

android {
    namespace = "com.example.lifelink"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.lifelink"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"



        multiDexEnabled true
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
















    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.cardview)
    implementation(libs.viewpager2) // Slideshow
    implementation(libs.recyclerview)

    implementation 'com.google.android.material:material:1.9.0'




    // ✅ Keep only the latest Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))

    // ✅ Firebase Libraries
    implementation("com.google.firebase:firebase-auth")       // Firebase Authentication
    implementation("com.google.firebase:firebase-firestore")  // Firestore Database
    implementation("com.google.firebase:firebase-database")   // Realtime Database
    implementation("com.google.firebase:firebase-analytics")  // Firebase Analytics

    // ✅ Google Sign-In (Required for Firebase Auth with Google)
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // ✅ Credential Manager Libraries (Keep ONLY if you're using Password-based sign-in)
    implementation("androidx.credentials:credentials:1.3.0")

    // Google Identity API (Keep if using Google Sign-In with Credential Manager)
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")


//email auth

    implementation 'com.github.danysantiago:smtp-client:1.4.0'












    // Testing Dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}
