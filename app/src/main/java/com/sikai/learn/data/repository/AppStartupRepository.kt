package com.sikai.learn.data.repository

import com.sikai.learn.data.local.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStartupRepository @Inject constructor(
    private val userDao: UserDao,
    private val learningDao: LearningDao,
    private val providerDao: AiProviderDao
) {
    suspend fun seedIfNeeded() {
        if (userDao.getProfile() == null) userDao.upsert(UserProfileEntity())
        if (learningDao.questions(10, "Mathematics", 1).isEmpty()) {
            learningDao.insertSubjects(SeedData.subjects)
            learningDao.insertQuestions(SeedData.questions)
        }
        if (providerDao.providers().first().isEmpty()) providerDao.upsertProviders(SeedData.providers)
    }
}
