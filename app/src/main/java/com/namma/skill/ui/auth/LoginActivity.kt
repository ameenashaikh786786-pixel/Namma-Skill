package com.namma.skill.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.namma.skill.MainActivity
import com.namma.skill.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Remove forced sign-out to allow session persistence
        // auth.signOut()

        binding.btnLogin.setOnClickListener {
            val email = binding.editEmail.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()

            // Clear previous errors
            binding.inputLayoutEmail.error = null
            binding.inputLayoutPassword.error = null

            // ✅ Inline validation
            if (email.isEmpty()) {
                binding.inputLayoutEmail.error = "Please enter your email address"
                binding.editEmail.requestFocus()
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.inputLayoutEmail.error = "Please enter a valid email (e.g. name@gmail.com)"
                binding.editEmail.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.inputLayoutPassword.error = "Please enter your password"
                binding.editPassword.requestFocus()
                return@setOnClickListener
            }
            if (password.length < 6) {
                binding.inputLayoutPassword.error = "Password must be at least 6 characters"
                binding.editPassword.requestFocus()
                return@setOnClickListener
            }

            // ✅ Show loading state
            setLoading(true)

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        goToMain()
                    } else {
                        val exception = task.exception
                        val message = exception?.message ?: ""
                        
                        if (message.contains("no user") || message.contains("identifier") || message.contains("No user")) {
                            // 🚀 Auto-Register: If user doesn't exist, create account automatically
                            setLoading(true)
                            binding.btnLogin.text = "Creating Account..."
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(this) { regTask ->
                                    setLoading(false)
                                    if (regTask.isSuccessful) {
                                        // Create initial profile
                                        val newUser = auth.currentUser
                                        if (newUser != null) {
                                            val profile = com.namma.skill.data.model.UserProfile(
                                                id = newUser.uid,
                                                name = email.substringBefore("@"), // Default name from email
                                                imageUrl = ""
                                            )
                                            com.namma.skill.data.repository.SkillRepository.updateUserProfile(profile)
                                        }
                                        goToMain()
                                    } else {
                                        showError("❌ Failed to create account: ${regTask.exception?.localizedMessage}")
                                    }
                                }
                        } else {
                            setLoading(false)
                            val errorMsg = when {
                                message.contains("password") -> "❌ Wrong password. Please try again."
                                message.contains("network") -> "❌ No internet connection."
                                else -> "❌ Login failed: ${exception?.localizedMessage ?: "Try again"}"
                            }
                            showError(errorMsg)
                        }
                    }
                }
        }

        // ✅ Forgot Password
        binding.textForgotPassword.setOnClickListener {
            val email = binding.editEmail.text.toString().trim()
            if (email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        MaterialAlertDialogBuilder(this)
                            .setTitle("📧 Reset Link Sent!")
                            .setMessage("We sent a password reset link to $email. Please check your inbox.")
                            .setPositiveButton("OK", null)
                            .show()
                    } else {
                        Snackbar.make(binding.root, "Could not send reset email. Check the address.", Snackbar.LENGTH_LONG).show()
                    }
                }
            } else {
                binding.inputLayoutEmail.error = "Enter your email above first"
                binding.editEmail.requestFocus()
            }
        }

        // ✅ Sign Up — points to RegisterActivity
        binding.textSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("OK", null)
            .show()
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !loading
        binding.btnLogin.text = if (loading) "Signing in…" else "Login  →"
        binding.editEmail.isEnabled = !loading
        binding.editPassword.isEnabled = !loading
    }

    private fun goToMain() {
        com.namma.skill.data.repository.SkillRepository.loadUserProfile()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
