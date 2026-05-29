plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    // Garde bien ce namespace si tes ressources y sont liées,
    // mais n'oublie pas de mettre le chemin complet (fr.projet.pulseo.MainActivity) dans le Manifest !
    namespace = "com.pulseo"
    compileSdk = 34 // Modifié de 36 à 34 (Android 14 - Stable)

    defaultConfig {
        applicationId = "com.pulseo"
        minSdk = 24
        targetSdk = 34 // Modifié de 36 à 34 (Android 14 - Stable)
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
}