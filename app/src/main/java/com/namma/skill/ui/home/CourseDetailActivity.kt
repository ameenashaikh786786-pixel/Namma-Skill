package com.namma.skill.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.namma.skill.data.repository.SkillRepository
import com.namma.skill.databinding.ActivityCourseDetailBinding
import com.namma.skill.ui.profile.EditProfileActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CourseDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCourseDetailBinding
    private val repository = SkillRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar setup
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Get data from intent
        val id = intent.getStringExtra("EXTRA_ID") ?: ""
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "Course Details"
        val trade = intent.getStringExtra("EXTRA_TRADE") ?: ""
        val duration = intent.getStringExtra("EXTRA_DURATION") ?: ""
        val eligibility = intent.getStringExtra("EXTRA_ELIGIBILITY") ?: ""
        val description = intent.getStringExtra("EXTRA_DESCRIPTION") ?: ""
        val center = intent.getStringExtra("EXTRA_CENTER") ?: ""
        val imageUrl = intent.getStringExtra("EXTRA_IMAGE_URL") ?: ""

        // Populate views
        binding.textDetailTitle.text = title
        binding.textDetailTrade.text = trade
        binding.chipDetailDuration.text = duration
        binding.chipDetailEligibility.text = eligibility
        binding.textDetailDescription.text = description
        binding.textDetailCenter.text = center

        // Debug Toast to verify data receipt
        android.widget.Toast.makeText(this, "Course: $title\nDuration: $duration\nElig: $eligibility", android.widget.Toast.LENGTH_LONG).show()

        Glide.with(this)
            .load(imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(binding.imageCourseHeader)

        val centerId = intent.getStringExtra("EXTRA_CENTER_ID") ?: ""

        // Call Center Detail
        binding.btnCallCenterDetail.setOnClickListener {
            lifecycleScope.launch {
                val center = repository.getCenters().first().find { it.id == centerId }
                center?.let {
                    val sanitizedContact = it.contact.replace(" ", "").replace("-", "").trim()
                    val dialIntent = Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:$sanitizedContact"))
                    startActivity(dialIntent)
                }
            }
        }

        // Display Center Contact Number explicitly
        lifecycleScope.launch {
            val centerObj = repository.getCenters().first().find { it.id == centerId }
            binding.textDetailPhone.text = "Contact Center: ${centerObj?.contact ?: "Not Available"}"
        }

        // FAB Click
        binding.fabApply.setOnClickListener {
            lifecycleScope.launch {
                val profile = repository.getUserProfile().first()
                if (profile.education.isEmpty() || profile.phone.isEmpty() || profile.district.isEmpty()) {
                    MaterialAlertDialogBuilder(this@CourseDetailActivity)
                        .setTitle("Profile Incomplete")
                        .setMessage("To generate a Candidate Summary for the center, please complete your profile first.")
                        .setPositiveButton("Complete Now") { _, _ ->
                            val editIntent = Intent(this@CourseDetailActivity, EditProfileActivity::class.java)
                            startActivity(editIntent)
                        }
                        .setNegativeButton("Maybe Later", null)
                        .show()
                } else {
                    val intent = Intent(this@CourseDetailActivity, CandidateSummaryActivity::class.java).apply {
                        putExtra("EXTRA_ID", id)
                        putExtra("EXTRA_TITLE", title)
                        putExtra("EXTRA_CENTER", center)
                    }
                    startActivity(intent)
                }
            }
        }
    }
}
