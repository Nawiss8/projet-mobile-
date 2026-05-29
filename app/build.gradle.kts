plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.pulseo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pulseo"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")

    // Exclure play-services-vision-common pour éviter les duplicates
    implementation("com.google.android.gms:play-services-vision:20.0.0") {
        exclude(group = "com.google.android.gms", module = "play-services-vision-common")
    }
}

configurations {
    all {
        resolutionStrategy {
            // Forcer la résolution des versions conflictuelles
            force("com.google.android.gms:play-services-vision:20.0.0")
            exclude(group = "com.google.android.gms", module = "play-services-vision-common")
        }
    }
}