plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.objectscanner"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.objectscanner"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val storeFilePath = System.getenv("ORG_GRADLE_PROJECT_storeFile") ?: project.findProperty("storeFile") as String?
            val storePassword = System.getenv("ORG_GRADLE_PROJECT_storePassword") ?: project.findProperty("storePassword") as String?
            val keyAlias = System.getenv("ORG_GRADLE_PROJECT_keyAlias") ?: project.findProperty("keyAlias") as String?
            val keyPassword = System.getenv("ORG_GRADLE_PROJECT_keyPassword") ?: project.findProperty("keyPassword") as String?
            if (storeFilePath != null && storePassword != null && keyAlias != null && keyPassword != null) {
                storeFile = file(storeFilePath)
                this.storePassword = storePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a")
            isUniversalApk = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation ("com.google.mlkit:image-labeling-custom:17.0.3")

    //gson
    implementation("com.google.code.gson:gson:2.8.9")
}