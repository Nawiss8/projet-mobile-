package com.pulseo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // Affiche l'email de l'utilisateur connecté
        val currentUser = auth.currentUser
        tvWelcome.text = "Welcome, ${currentUser?.email ?: "User"} !"

        btnLogout.setOnClickListener {
            auth.signOut()
            finish() // Retour à MainActivity
        }
    }
}