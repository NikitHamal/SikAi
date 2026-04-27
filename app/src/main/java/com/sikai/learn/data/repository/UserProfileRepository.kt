package com.sikai.learn.data.repository

import com.sikai.learn.data.local.SubjectDao
import com.sikai.learn.data.local.SubjectEntity
import com.sikai.learn.data.local.UserProfileDao
import com.sikai.learn.data.local.UserProfileEntity
import com.sikai.learn.domain.model.NepalCurriculum
import com.sikai.learn.domain.model.StudentProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepository @Inject constructor(
    private val profileDao: UserProfileDao,
    private val subjectDao: SubjectDao,
) {

    fun observe(): Flow<StudentProfile?> = profileDao.observe().map { it?.toDomain() }

    suspend fun current(): StudentProfile? = profileDao.get()?.toDomain()

    suspend fun isOnboarded(): Boolean = profileDao.get() != null

    suspend fun completeOnboarding(profile: StudentProfile) {
        profileDao.upsert(
            UserProfileEntity(
                classLevel = profile.classLevel,
                subjectIdsCsv = profile.subjects.joinToString(","),
                language = profile.language,
                examDateMillis = profile.examDateMillis,
                studyMinutesPerDay = profile.studyMinutesPerDay,
                onboardedAtMillis = System.currentTimeMillis(),
            )
        )
        // Mirror curriculum subjects into the subject table for fast lookups
        val seeds = NepalCurriculum.subjectsFor(profile.classLevel)
            .filter { it.id in profile.subjects }
            .map { SubjectEntity(it.id, it.displayName, profile.classLevel) }
        if (seeds.isNotEmpty()) subjectDao.upsertAll(seeds)
    }

    suspend fun bumpStreakAndXp(streak: Int, xp: Int) {
        profileDao.updateStreak(streak, xp)
    }
}

private fun UserProfileEntity.toDomain() = StudentProfile(
    classLevel = classLevel,
    subjects = subjectIdsCsv.split(",").filter { it.isNotBlank() },
    language = language,
    examDateMillis = examDateMillis,
    studyMinutesPerDay = studyMinutesPerDay,
)
