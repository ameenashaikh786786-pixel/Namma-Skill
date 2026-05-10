package com.namma.skill.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.namma.skill.MainActivity
import com.namma.skill.data.model.UserProfile
import com.namma.skill.data.repository.SkillRepository
import com.namma.skill.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnRegister.setOnClickListener {
            val name = binding.editName.text.toString().trim()
            val email = binding.editEmail.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()
            val confirmPassword = binding.editConfirmPassword.text.toString().trim()

            // Clear errors
            binding.inputLayoutName.error = null
            binding.inputLayoutEmail.error = null
            binding.inputLayoutPassword.error = null
            binding.inputLayoutConfirmPassword.error = null

            if (name.isEmpty()) {
                binding.inputLayoutName.error = "Please enter your full name"
                binding.editName.requestFocus()
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                binding.inputLayoutEmail.error = "Please enter your email"
                binding.editEmail.requestFocus()
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.inputLayoutEmail.error = "Please enter a valid email"
                binding.editEmail.requestFocus()
                return@setOnClickListener
            }
            if (password.length < 6) {
                binding.inputLayoutPassword.error = "Password must be at least 6 characters"
                binding.editPassword.requestFocus()
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                binding.inputLayoutConfirmPassword.error = "Passwords do not match"
                binding.editConfirmPassword.requestFocus()
                return@setOnClickListener
            }

            setLoading(true)

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        if (firebaseUser != null) {
                            // Create a basic profile for the new user
                            val newProfile = UserProfile(
                                id = firebaseUser.uid,
                                name = name,
                                imageUrl = ""
                            )
                            SkillRepository.updateUserProfile(newProfile)
                            goToMain()
                        }
                    } else {
                        setLoading(false)
                        val errorMsg = task.exception?.message ?: "Registration failed. Try again."
                        Snackbar.make(binding.root, "❌ $errorMsg", Snackbar.LENGTH_LONG).show()
                    }
                }
        }

        binding.textBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !loading
        binding.btnRegister.text = if (loading) "Creating account..." else "Create Account  →"
        binding.editName.isEnabled = !loading
        binding.editEmail.isEnabled = !loading
        binding.editPassword.isEnabled = !loading
        binding.editConfirmPassword.isEnabled = !loading
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
