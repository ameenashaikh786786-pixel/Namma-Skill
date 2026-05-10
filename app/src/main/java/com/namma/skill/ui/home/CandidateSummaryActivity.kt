package com.namma.skill.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.namma.skill.data.repository.SkillRepository
import com.namma.skill.databinding.ActivityCandidateSummaryBinding
import com.namma.skill.util.NotificationHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collect

class CandidateSummaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCandidateSummaryBinding
    private val repository = SkillRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCandidateSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val courseId = intent.getStringExtra("EXTRA_ID") ?: ""
        val courseTitle = intent.getStringExtra("EXTRA_TITLE") ?: ""
        val centerName = intent.getStringExtra("EXTRA_CENTER") ?: ""

        // Fetch Real User Data from Repository
        lifecycleScope.launchWhenStarted {
            repository.getUserProfile().collect { profile ->
                binding.textCandidateName.text = getString(com.namma.skill.R.string.candidate_name_label, profile.name)
                binding.textCandidatePhone.text = getString(com.namma.skill.R.string.candidate_phone_label, profile.phone)
                binding.textCandidateEdu.text = getString(com.namma.skill.R.string.candidate_edu_label, profile.education)
                binding.textCandidateExp.text = "💼 Experience: ${profile.experience} years"
            }
        }

        binding.textCourseSummary.text = "📚 Applying for: $courseTitle\n🏫 at $centerName"

        binding.btnConfirmApply.setOnClickListener {
            // Save application to repository + Firebase
            repository.applyForCourse(courseId)

            // Show success local notification
            NotificationHelper(this).showNewBatchNotification(courseTitle, "Application Confirmed")

            // ✅ Show friendly success dialog instead of just closing
            lifecycleScope.launchWhenStarted {
                repository.getUserProfile().collect { profile ->
                    MaterialAlertDialogBuilder(this@CandidateSummaryActivity)
                        .setTitle(getString(com.namma.skill.R.string.apply_success_title))
                        .setMessage(
                            getString(
                                com.namma.skill.R.string.apply_success_message,
                                profile.name,
                                courseTitle
                            )
                        )
                        .setPositiveButton(getString(com.namma.skill.R.string.btn_ok_great)) { _, _ ->
                            finish()
                        }
                        .setCancelable(false)
                        .show()
                }
            }
        }
    }
}
