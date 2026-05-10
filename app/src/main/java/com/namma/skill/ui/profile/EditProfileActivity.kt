package com.namma.skill.ui.profile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.namma.skill.data.model.UserProfile
import com.namma.skill.data.repository.SkillRepository
import com.namma.skill.databinding.ActivityEditProfileBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import com.bumptech.glide.Glide
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.activity.result.contract.ActivityResultContracts

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val repository = SkillRepository
    private var selectedImageUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Load current data
        lifecycleScope.launch {
            val profile = repository.getUserProfile().first()
            binding.apply {
                val defaultName = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email?.substringBefore("@") ?: ""
                editName.setText(profile.name.ifBlank { defaultName })
                editPhone.setText(profile.phone)
                editEducation.setText(profile.education)
                editExperience.setText(profile.experience)
                editDistrict.setText(profile.district)
                selectedImageUri = profile.imageUrl

                if (profile.imageUrl.isNotBlank()) {
                    Glide.with(this@EditProfileActivity)
                        .load(profile.imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(imageEditProfile)
                }

                // Set checked chips
                for (i in 0 until chipGroupTrades.childCount) {
                    val chip = chipGroupTrades.getChildAt(i) as? com.google.android.material.chip.Chip
                    if (chip != null && profile.favoriteTrades.any { chip.text.toString().contains(it) || it.contains(it) }) {
                        // Match by checking if trade name is in chip text
                        val chipTrade = chip.text.toString()
                            .replace("⚡ ", "").replace("🧵 ", "")
                            .replace("💻 ", "").replace("☀️ ", "")
                        chip.isChecked = profile.favoriteTrades.contains(chipTrade)
                    }
                }
            }
            updateProgress()
        }

        binding.imageEditProfile.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // ✅ Real-time progress update as user types
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = updateProgress()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        binding.editName.addTextChangedListener(watcher)
        binding.editPhone.addTextChangedListener(watcher)
        binding.editEducation.addTextChangedListener(watcher)
        binding.editExperience.addTextChangedListener(watcher)
        binding.editDistrict.addTextChangedListener(watcher)

        binding.btnSaveProfile.setOnClickListener {
            val name = binding.editName.text.toString().trim()
            val phone = binding.editPhone.text.toString().trim()
            val education = binding.editEducation.text.toString().trim()
            val experience = binding.editExperience.text.toString().trim()
            val district = binding.editDistrict.text.toString().trim()

            // ✅ Clear previous errors
            binding.layoutName.error = null
            binding.layoutPhone.error = null

            // ✅ Friendly validation with inline field errors
            if (name.isEmpty()) {
                binding.layoutName.error = getString(com.namma.skill.R.string.error_name_required)
                binding.editName.requestFocus()
                return@setOnClickListener
            }
            if (phone.length != 10 || !phone.all { it.isDigit() }) {
                binding.layoutPhone.error = getString(com.namma.skill.R.string.error_phone_invalid)
                binding.editPhone.requestFocus()
                return@setOnClickListener
            }

            // Collect selected trades (strip emoji prefix)
            val selectedTrades = mutableListOf<String>()
            for (i in 0 until binding.chipGroupTrades.childCount) {
                val chip = binding.chipGroupTrades.getChildAt(i) as? com.google.android.material.chip.Chip
                if (chip != null && chip.isChecked) {
                    val tradeName = chip.text.toString()
                        .replace("⚡ ", "").replace("🧵 ", "")
                        .replace("💻 ", "").replace("☀️ ", "")
                    val topicName = "trade_${tradeName.replace(" ", "_").replace("/", "_")}"
                    selectedTrades.add(tradeName)
                    FirebaseMessaging.getInstance().subscribeToTopic(topicName)
                } else if (chip != null) {
                    val tradeName = chip.text.toString()
                        .replace("⚡ ", "").replace("🧵 ", "")
                        .replace("💻 ", "").replace("☀️ ", "")
                    val topicName = "trade_${tradeName.replace(" ", "_").replace("/", "_")}"
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(topicName)
                }
            }

            lifecycleScope.launch {
                val currentProfile = repository.getUserProfile().first()
                val updatedProfile = currentProfile.copy(
                    name = name,
                    phone = phone,
                    education = education,
                    experience = experience,
                    district = district,
                    imageUrl = selectedImageUri ?: currentProfile.imageUrl,
                    favoriteTrades = selectedTrades
                )
                repository.updateUserProfile(updatedProfile)

                // ✅ Friendly success SnackBar instead of plain Toast
                Snackbar.make(
                    binding.root,
                    getString(com.namma.skill.R.string.profile_saved_success),
                    Snackbar.LENGTH_LONG
                ).setAction("Great! ✅") { finish() }.show()
            }
        }
    }

    /** Calculates completion % based on 5 key fields (20% each) and updates the progress bar. */
    private fun updateProgress() {
        var filled = 0
        if (binding.editName.text.toString().isNotBlank()) filled++
        if (binding.editPhone.text.toString().length == 10) filled++
        if (binding.editEducation.text.toString().isNotBlank()) filled++
        if (binding.editExperience.text.toString().isNotBlank()) filled++
        if (binding.editDistrict.text.toString().isNotBlank()) filled++

        val percent = filled * 20
        binding.progressProfile.progress = percent
        binding.textProgressPercent.text =
            getString(com.namma.skill.R.string.profile_completion, percent)
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it.toString()
            Glide.with(this).load(it).into(binding.imageEditProfile)
        }
    }
}
