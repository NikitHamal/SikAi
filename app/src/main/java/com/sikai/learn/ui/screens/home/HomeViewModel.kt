package com.sikai.learn.ui.screens.home

import androidx.lifecycle.ViewModel
import com.sikai.learn.core.storage.UserPreferences
import com.sikai.learn.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class HomeState(
    val displayName: String = "Student",
    val classLevel: Int = 10,
    val streakDays: Int = 0,
    val xp: Int = 0,
    val examInDays: Long? = null,
    val nepaliMode: Boolean = false,
    val subjects: List<String> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepo: UserProfileRepository,
    private val prefs: UserPreferences,
) : ViewModel() {
    val state: Flow<HomeState> = combine(
        userRepo.observe(),
        prefs.streakDays,
        prefs.xp,
        prefs.examDate,
        prefs.subjects,
    ) { profile, streak, xp, examDate, subjects ->
        val nowDays = System.currentTimeMillis()
        val examInDays = examDate?.let { ((it - nowDays) / (24L * 60 * 60 * 1000)).coerceAtLeast(0) }
        HomeState(
            displayName = profile?.displayName ?: "Student",
            classLevel = profile?.classLevel ?: 10,
            streakDays = streak,
            xp = xp,
            examInDays = examInDays,
            nepaliMode = profile?.language == "ne",
            subjects = subjects,
        )
    }
}
