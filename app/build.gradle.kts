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
        debug {
            // Assure un packaging propre pour le mode debug
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
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

    // Firebase BoM (Gère toutes les versions Firebase et Play Services associées de manière stable)
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")

    // Si tu as absolument besoin de Play Services Vision, on l'inclut normalement.
    // Le BoM ou Gradle moderne va résoudre le conflit tout seul sans casser l'APK.
    implementation("com.google.android.gms:play-services-vision:20.1.3")
}

// SUPPRESSION du bloc "configurations" problématique qui corrompait l'alignement de l'APK