
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.businesscardexchange"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.businesscardexchange"
        minSdk = 24
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
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }

}

dependencies {

    implementation(libs.firebase.crashlytics.buildtools)
    dependencies {
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.appcompat)
        implementation(libs.material) // 或 implementation(libs.androidx.material3)
        implementation(libs.androidx.activity)
        implementation(libs.androidx.constraintlayout)
        implementation(libs.androidx.navigation.runtime.ktx)
        implementation(libs.androidx.navigation.ui.ktx)
        implementation(libs.androidx.navigation.fragment)
        implementation(libs.androidx.navigation.fragment.ktx)
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        implementation(libs.core)
        implementation(libs.barcode.scanning) // 或 implementation(libs.zxing.android.embedded)
        implementation(libs.androidx.camera.core)
        implementation(libs.androidx.camera.camera2)
        implementation(libs.androidx.camera.lifecycle)
        implementation(libs.androidx.camera.view)
        implementation(libs.androidx.room.runtime)
        //kapt(libs.androidx.room.compiler) // 使用 kapt 而不是 implementation
        // implementation(libs.guava)  // 如果没有明确需要，建议移除
        implementation(libs.zxing.android.embedded)
        implementation(libs.androidx.activity.ktx)
        implementation(libs.androidx.core.ktx.v1120)

    }
}