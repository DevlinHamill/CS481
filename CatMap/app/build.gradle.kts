plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "edu.cwu.catmap"
    compileSdk = 34

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
}

dependencies {

    //desugaring to use Java.time module
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    //auto-generated
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    //firebase API
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.analytics)

    //google sign-in API
    implementation(libs.play.services.auth)

    //google maps API
    implementation(libs.play.services.maps)

    // Google Maps Directions API (use Retrofit for API calls)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    //google calendar API
    //implementation(libs.google.api.services.calendar)
    //implementation(libs.google.auth.library.oauth2.http)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}