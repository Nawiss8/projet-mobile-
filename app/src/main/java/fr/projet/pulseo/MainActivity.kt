package com.pulseo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    // 1. Déclarer Firebase Auth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 2. Initialiser Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Optionnel : Si l'utilisateur est déjà connecté, on peut l'envoyer direct au lecteur de musique
        if (auth.currentUser != null) {
            // startActivity(Intent(this, HomeMusicActivity::class.java))
            // finish()
        }

        try {
            val etEmail = findViewById<EditText>(R.id.etEmail)
            val etPassword = findViewById<EditText>(R.id.etPassword)
            val btnLogin = findViewById<Button>(R.id.btnLogin)
            val tvGoRegister = findViewById<TextView>(R.id.tvGoRegister)

            btnLogin.setOnClickListener {
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // 3. Connexion réelle avec Firebase
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Welcome to Pulseo !", Toast.LENGTH_SHORT).show()

                            // TODO: Remplace "RegisterActivity" par la classe de ton lecteur de musique (ex: HomeActivity)
                            // startActivity(Intent(this, HomeMusicActivity::class.java))
                            // finish()
                        } else {
                            Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }

            tvGoRegister.setOnClickListener {
                startActivity(Intent(this, RegisterActivity::class.java))
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}