package com.pulseo

import android.app.Application
import com.google.firebase.FirebaseApp

class PulseoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}