plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "edu.cwu.catmap"
    compileSdk = 35

    defaultConfig {
        applicationId = "edu.cwu.catmap"
        minSdk = 23
        targetSdk = 34
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
        isCoreLibraryDesugaringEnabled = true
    }

    viewBinding {
        enable = true
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.play.services.maps)
    implementation(libs.preference)

    implementation(libs.androidx.appcompat)
    implementation(libs.roundedimageview)
    implementation(libs.google.material)
    implementation(libs.volley)


    //desugaring to use Java.time module
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    //firebase dependencies
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.analytics)
    //google sign-in dependencies
    implementation(libs.play.services.auth)

    //google Calendar API
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.github.QuadFlask:colorpicker:0.0.15")
    implementation("com.google.android.gms:play-services-location:21.1.0")
    implementation("com.google.android.material:material:1.11.0")

    implementation(libs.retrofit)
    implementation(libs.converter.gson)




}