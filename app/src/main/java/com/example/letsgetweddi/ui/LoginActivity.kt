package com.example.letsgetweddi.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.letsgetweddi.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = auth.currentUser!!.uid
                            FirebaseDatabase.getInstance().getReference("Users")
                                .child(uid)
                                .get()
                                .addOnSuccessListener { snapshot ->
                                    val userType = snapshot.child("userType").value.toString()
                                    if (userType == "Supplier") {
                                        startActivity(Intent(this, MainActivity::class.java).apply {
                                            putExtra("userType", "Supplier")
                                        })
                                    } else {
                                        startActivity(Intent(this, MainActivity::class.java).apply {
                                            putExtra("userType", "Client")
                                        })
                                    }
                                    finish()
                                }
                        } else {
                            Toast.makeText(this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        binding.buttonRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
