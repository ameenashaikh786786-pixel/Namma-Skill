package com.namma.skill

import android.app.Application
import com.namma.skill.data.repository.SkillRepository

class NammaSkillApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize the singleton repository once for the entire app life
        SkillRepository.init(this)
    }
}
