package com.sikai.learn.data.seed

import com.sikai.learn.data.db.dao.PastPaperDao
import com.sikai.learn.data.db.dao.QuestionDao
import com.sikai.learn.data.db.dao.SubjectDao
import com.sikai.learn.data.repository.ContentManifestRepository
import com.sikai.learn.data.repository.ProviderConfigRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Idempotent first-launch seeder. Runs on app startup, populating subjects, sample
 * questions, sample past paper rows, the seed manifest from assets, and the built-in
 * AI provider configs (Qwen, DeepInfra, Gemini, OpenRouter, NVIDIA, DeepSeek).
 */
@Singleton
class SeedManager @Inject constructor(
    private val subjectDao: SubjectDao,
    private val questionDao: QuestionDao,
    private val pastPaperDao: PastPaperDao,
    private val providerRepo: ProviderConfigRepository,
    private val manifestRepo: ContentManifestRepository,
) {
    suspend fun seedAll() {
        runCatching { subjectDao.insertAll(SeedData.subjects()) }
        if (questionDao.count() == 0) {
            runCatching { questionDao.insertAll(SeedData.seedQuestions()) }
        }
        runCatching { pastPaperDao.insertAll(SeedData.seedPastPapers()) }
        runCatching { providerRepo.seedBuiltInsIfMissing() }
        runCatching { manifestRepo.seedFromAssetsIfEmpty() }
    }
}
