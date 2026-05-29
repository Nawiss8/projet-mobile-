package com.pulseo

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    // 1. Déclarer Firebase Auth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // 2. Initialiser Firebase Auth
        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvGoLogin = findViewById<TextView>(R.id.tvGoLogin)

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Remplis tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Le mot de passe doit faire au moins 6 caractères", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. Connexion réelle à Firebase pour créer l'utilisateur
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Compte créé avec succès ! 🎉", Toast.LENGTH_SHORT).show()
                        finish() // Retour à l'écran de Login
                    } else {
                        Toast.makeText(this, "Erreur : ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        tvGoLogin.setOnClickListener {
            finish()
        }
    }
}