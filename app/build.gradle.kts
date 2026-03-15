plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.kiddo"
    compileSdk = 36

    defaultConfig {
        applicationId = (project.findProperty("KIDDO_APP_ID") as String?) ?: "com.example.kiddo"
        minSdk = 24
        targetSdk = 36
        versionCode = ((project.findProperty("KIDDO_VERSION_CODE") as String?)?.toIntOrNull()) ?: 2
        versionName = (project.findProperty("KIDDO_VERSION_NAME") as String?) ?: "1.0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val keystorePath = project.findProperty("KIDDO_KEYSTORE_PATH") as String?
    val keystorePassword = project.findProperty("KIDDO_KEYSTORE_PASSWORD") as String?
    val keyAlias = project.findProperty("KIDDO_KEY_ALIAS") as String?
    val keyPassword = project.findProperty("KIDDO_KEY_PASSWORD") as String?

    signingConfigs {
        if (
            !keystorePath.isNullOrBlank() &&
            !keystorePassword.isNullOrBlank() &&
            !keyAlias.isNullOrBlank() &&
            !keyPassword.isNullOrBlank()
        ) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = keystorePassword
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
            signingConfigs.findByName("release")?.let { signingConfig = it }
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
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    testImplementation(libs.junit)
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
