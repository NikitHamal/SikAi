package com.sikai.learn.data.repository

import com.sikai.learn.core.storage.UserPreferences
import com.sikai.learn.data.db.dao.UserProfileDao
import com.sikai.learn.data.db.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepository @Inject constructor(
    private val dao: UserProfileDao,
    private val prefs: UserPreferences,
) {
    fun observe(): Flow<UserProfileEntity?> = dao.observe()
    suspend fun get(): UserProfileEntity? = dao.get()

    suspend fun completeOnboarding(
        classLevel: Int,
        language: String,
        subjects: List<String>,
        examDate: Long?,
    ) {
        prefs.setClassLevel(classLevel)
        prefs.setLanguage(language)
        prefs.setSubjects(subjects)
        prefs.setExamDate(examDate)
        prefs.setOnboarded(true)
        prefs.setNepaliMode(language == "ne")

        val current = dao.get() ?: UserProfileEntity()
        dao.upsert(
            current.copy(
                classLevel = classLevel,
                language = language,
                subjectsCsv = subjects.joinToString(","),
                examDate = examDate,
                onboarded = true,
                lastActiveAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun bumpStreakAndXp(xpDelta: Int) {
        val now = System.currentTimeMillis()
        val current = dao.get() ?: UserProfileEntity()
        val oneDay = 24 * 60 * 60 * 1000L
        val lastActive = current.lastActiveAt
        val keptStreak = lastActive > 0 && (now - lastActive) < (2 * oneDay)
        val newStreak = if (keptStreak) {
            if ((now - lastActive) < oneDay) current.streakDays else current.streakDays + 1
        } else 1
        dao.upsert(
            current.copy(
                streakDays = newStreak,
                xp = current.xp + xpDelta,
                lastActiveAt = now,
            ),
        )
        prefs.setStreak(newStreak)
        prefs.setXp(current.xp + xpDelta)
        prefs.setLastActiveAt(now)
    }
}
