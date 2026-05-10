package com.namma.skill.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.namma.skill.data.model.Course
import com.namma.skill.data.model.Center
import com.namma.skill.data.model.SuccessStory
import com.namma.skill.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach

object SkillRepository {

    private val firestore by lazy {
        try {
            FirebaseFirestore.getInstance().also { db ->
                // ✅ Enable offline caching so courses load without internet (rural students!)
                val settings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                    .build()
                db.firestoreSettings = settings
            }
        } catch (e: Exception) { null }
    }
    private val useFirebase get() = firestore != null
    private var prefs: SharedPreferences? = null

    private val appliedCourseIds = MutableStateFlow<Set<String>>(emptySet())
    private val _currentUserProfile = MutableStateFlow<UserProfile>(UserProfile())

    // Comprehensive Mock Data — always available as instant fallback
    private val mockCourses = listOf(
        // Electrician
        Course("elec_01", "Basic Electrical Wiring", "Electrician", "c1", "Hosur Skill Center", "3 Months", true, true, "2024-06-01", "10th Pass", "Learn home wiring, safety protocols, and basic electrical maintenance.", "android.resource://com.namma.skill/drawable/course_electrician"),
        Course("elec_02", "Industrial Panel Wiring", "Electrician", "c1", "Hosur Skill Center", "6 Months", false, true, "2024-07-15", "ITI/12th Pass", "Advanced training in industrial control panels, heavy machinery wiring, and automation.", "android.resource://com.namma.skill/drawable/course_electrician"),
        
        // Welding
        Course("3", "Basic Welding & Fabrication", "Welding", "c1", "Hosur Skill Center", "3 Months", true, true, "2024-06-01", "8th Pass", "Learn industrial ARC and Gas welding. Focus on safety and structural fabrication.", "android.resource://com.namma.skill/drawable/course_welding"),
        Course("4", "Advanced Pipe Welding", "Welding", "c2", "Mandya District Center", "6 Months", false, true, "2024-08-10", "10th Pass", "Specialized training in pipe welding and TIG/MIG techniques for industrial projects.", "android.resource://com.namma.skill/drawable/course_welding"),

        // Coding
        Course("5", "Web Development Basics", "Coding", "c3", "Ramanagara Training Hub", "3 Months", true, true, "2024-06-15", "12th Pass", "Introduction to HTML, CSS, and JavaScript. Build your first website in 90 days.", "android.resource://com.namma.skill/drawable/course_coding"),
        Course("6", "Advanced Web Development", "Coding", "c3", "Ramanagara Training Hub", "6 Months", false, true, "2024-09-01", "Graduation", "Full-stack development with modern frameworks. Includes internship and job placement.", "android.resource://com.namma.skill/drawable/course_coding"),
        
        // Sewing
        Course("7", "Basic Fashion Tailoring", "Sewing", "c1", "Hosur Skill Center", "3 Months", true, true, "2024-06-01", "8th Pass", "Master the basics of cutting, stitching, and garment construction.", "android.resource://com.namma.skill/drawable/course_sewing"),
        Course("8", "Industrial Garment Making", "Sewing", "c2", "Mandya District Center", "6 Months", false, true, "2024-07-20", "10th Pass", "Training for garment industry roles with focus on speed and quality control.", "android.resource://com.namma.skill/drawable/course_sewing"),
        
        // Mobile Repair
        Course("9", "Basic Smartphone Repair", "Mobile Repair", "c3", "Ramanagara Training Hub", "3 Months", true, true, "2024-06-10", "10th Pass", "Learn to troubleshoot and repair hardware and software issues in smartphones.", "android.resource://com.namma.skill/drawable/course_mobile"),
        Course("10", "Advanced Mobile Chip Level", "Mobile Repair", "c1", "Hosur Skill Center", "6 Months", false, true, "2024-08-05", "12th Pass", "Micro-soldering and advanced PCB level repairs for flagship smartphones.", "android.resource://com.namma.skill/drawable/course_mobile")
    )

    // ✅ Pre-seeded with mockCourses in init() so getAppliedCourses() always
    //    has data to filter immediately — without waiting for a Firebase response.
    private val _allCoursesCached = MutableStateFlow<List<Course>>(emptyList())


    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.applicationContext.getSharedPreferences("skill_prefs", Context.MODE_PRIVATE)

        // ✅ Seed cache with mock courses immediately
        _allCoursesCached.value = mockCourses

        loadUserProfile()
    }

    fun loadUserProfile() {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid

        // Try Firestore first, then fallback to local prefs
        if (useFirebase) {
            firestore?.collection("users")?.document(uid)?.get()?.addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val firestoreProfile = doc.toObject(UserProfile::class.java)
                    if (firestoreProfile != null) {
                        val currentLocal = _currentUserProfile.value
                        // Merge: Use local imageUrl if Firestore one is blank (for local path persistence)
                        val mergedProfile = firestoreProfile.copy(
                            id = uid,
                            imageUrl = if (firestoreProfile.imageUrl.isBlank()) currentLocal.imageUrl else firestoreProfile.imageUrl
                        )
                        _currentUserProfile.value = mergedProfile
                    }
                } else {
                    // Initialize with basic info if first time
                    val initialName = user.displayName ?: user.email?.substringBefore("@") ?: "Student"
                    val initialProfile = UserProfile(id = uid, name = initialName)
                    _currentUserProfile.value = initialProfile
                    updateUserProfile(initialProfile)
                }
            }
        }

        prefs?.let { p ->
            val defaultName = user.displayName ?: user.email?.substringBefore("@") ?: "Student"
            val name = p.getString("${uid}_name", defaultName) ?: defaultName
            val phone = p.getString("${uid}_phone", "") ?: ""
            val edu = p.getString("${uid}_education", "") ?: ""
            val exp = p.getString("${uid}_experience", "") ?: ""
            val district = p.getString("${uid}_district", "") ?: ""
            val imageUrl = p.getString("${uid}_image_url", "") ?: ""
            val trades = p.getStringSet("${uid}_trades", emptySet())?.toList() ?: emptyList()

            _currentUserProfile.value = UserProfile(uid, name, phone, edu, exp, district, imageUrl, trades)
            appliedCourseIds.value = p.getStringSet("${uid}_applied_ids", emptySet()) ?: emptySet()
        }

        // Sync applications from Firestore
        if (useFirebase) {
            firestore?.collection("applications")
                ?.whereEqualTo("userId", uid)
                ?.addSnapshotListener { snapshot, _ ->
                    val ids = snapshot?.documents?.mapNotNull { it.getString("courseId") }?.toSet() ?: emptySet()
                    if (ids.isNotEmpty()) {
                        appliedCourseIds.value = appliedCourseIds.value + ids
                        // Also update local cache
                        prefs?.edit { putStringSet("${uid}_applied_ids", appliedCourseIds.value) }
                    }
                }
        }
    }

    fun getCourses(): Flow<List<Course>> {
        val flow = if (useFirebase) {
            callbackFlow {
                val subscription = firestore?.collection("courses")
                    ?.addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(mockCourses)
                            return@addSnapshotListener
                        }
                        val courses = snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(Course::class.java)?.copy(id = doc.id)
                        } ?: emptyList()
                        // Always include mock courses as base data, but apply a target filter
                        // to remove the specific courses the user wants gone.
                        val combined = (mockCourses + courses)
                            .distinctBy { it.id }
                            .filterNot { it.title.contains("Electrician Training", ignoreCase = true) && it.centerName.contains("Hosur", ignoreCase = true) }
                        trySend(combined)
                    }
                awaitClose { subscription?.remove() }
            }
        } else {
            flowOf(mockCourses.filterNot { it.title.contains("Electrician Training", ignoreCase = true) && it.centerName.contains("Hosur", ignoreCase = true) })
        }
        return flow.onEach { _allCoursesCached.value = it }
    }

    fun getCenters(): Flow<List<Center>> {
        val allCenters = listOf(
            Center("c1", "Hosur Skill Center", "Near Bus Stand, Hosur", "+91 9845012345", 12.74, 77.82, "Krishnagiri", "android.resource://com.namma.skill/drawable/center_exterior"),
            Center("c2", "Mandya District Center", "Kuvempu Nagar, Mandya", "+91 9900054321", 12.52, 76.90, "Mandya", "android.resource://com.namma.skill/drawable/course_sewing"),
            Center("c3", "Ramanagara Training Hub", "Main Road, Ramanagara", "+91 9123456789", 12.72, 77.27, "Ramanagara", "android.resource://com.namma.skill/drawable/course_coding")
        )
        return flowOf(allCenters)
    }

    fun getSuccessStories(): Flow<List<SuccessStory>> = flowOf(listOf(
        SuccessStory("s1", "Ramesh Kumar", "Welding", "I now work as a certified welder for a major industrial plant. This course changed my life!", "L&T Construction", "Krishnagiri", "https://images.unsplash.com/photo-1531983412531-1f49a365ffed?auto=format&fit=crop&w=800"),
        SuccessStory("s2", "Anitha S.", "Mobile Repair", "I opened my own mobile service shop after the 3-month course. I am now independent!", "Self-Employed", "Bengaluru", "https://images.unsplash.com/photo-1591799264318-7e6ef8ddb7ea?auto=format&fit=crop&w=800"),
        SuccessStory("s3", "Suresh V.", "Electrician", "Mastering industrial wiring helped me land a job at a top manufacturing unit in Hosur.", "Tata Motors", "Hosur", "https://images.unsplash.com/photo-1581094794329-c8112a89af12?auto=format&fit=crop&w=800"),
        SuccessStory("s4", "Megha R.", "Coding", "I never thought I could code. Now I am a junior web developer in a Bengaluru startup!", "TechNova Solutions", "Bengaluru", "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=800")
    ))

    fun getUserProfile(): Flow<UserProfile> = _currentUserProfile

    fun updateUserProfile(profile: UserProfile) {
        val uid = profile.id.ifEmpty { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "" }
        if (uid.isEmpty()) return

        val finalProfile = profile.copy(id = uid)
        _currentUserProfile.value = finalProfile
        
        prefs?.edit {
            putString("${uid}_name", finalProfile.name)
            putString("${uid}_phone", finalProfile.phone)
            putString("${uid}_education", finalProfile.education)
            putString("${uid}_experience", finalProfile.experience)
            putString("${uid}_district", finalProfile.district)
            putString("${uid}_image_url", finalProfile.imageUrl)
            putStringSet("${uid}_trades", finalProfile.favoriteTrades.toSet())
        }
        if (useFirebase) {
            firestore?.collection("users")?.document(uid)?.set(finalProfile)
        }
    }

    fun applyForCourse(courseId: String) {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid
        val currentIds = appliedCourseIds.value
        if (courseId !in currentIds) {
            val newIds = currentIds + courseId
            appliedCourseIds.value = newIds
            prefs?.edit { putStringSet("${uid}_applied_ids", newIds) }
            
            if (useFirebase) {
                val profile = _currentUserProfile.value
                val application = hashMapOf(
                    "courseId" to courseId,
                    "userId" to profile.id,
                    "userName" to profile.name,
                    "timestamp" to com.google.firebase.Timestamp.now()
                )
                firestore?.collection("applications")?.add(application)
            }
        }
    }

    fun requestCallback(course: Course) {
        if (useFirebase) {
            val profile = _currentUserProfile.value
            val request = hashMapOf(
                "candidateName" to profile.name,
                "candidatePhone" to profile.phone,
                "courseTitle" to course.title,
                "status" to "Pending"
            )
            firestore?.collection("callback_requests")?.add(request)
        }
    }

    fun getAppliedCourses(): Flow<List<Course>> {
        // ✅ Uses _allCoursesCached which is pre-seeded with mockCourses in init().
        // This guarantees an instant, correct result from ANY screen without waiting
        // for a cold Firebase flow to fire. Firebase updates the cache in getCourses().
        return combine(appliedCourseIds, _allCoursesCached) { ids, courses ->
            courses.filter { it.id in ids }
        }
    }
}
