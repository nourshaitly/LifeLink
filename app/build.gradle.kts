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

    implementation(platform(libs.firebase.bom))  // Firebase BOM for version management
    implementation(libs.firebase.firestore)     // Firebase Firestore
    implementation("com.google.firebase:firebase-auth:21.0.1")
    // Firebase Authentication
    implementation(libs.google.firebase.analytics)
    implementation(libs.firebase.database)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid) // Firebase Analytics

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // slideshow

        implementation(libs.androidx.core)
        implementation(libs.appcompat)
        implementation(libs.constraintlayout)
        implementation(libs.viewpager2) // Slideshow
        implementation(libs.recyclerview)




    // Firebase BOM (Bill of Materials) - automatically manages the version of Firebase libraries
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))

    // Firebase Authentication - Kotlin extensions
    implementation("com.google.firebase:firebase-auth-ktx")










    // Also add the dependencies for the Credential Manager libraries and specify their versions
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")


}
