package com.sikai.learn.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profiles WHERE id = 'local'") fun profile(): Flow<UserProfileEntity?>
    @Query("SELECT * FROM user_profiles WHERE id = 'local'") suspend fun getProfile(): UserProfileEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(profile: UserProfileEntity)
}

@Dao
interface LearningDao {
    @Query("SELECT * FROM subjects ORDER BY name") fun subjects(): Flow<List<SubjectEntity>>
    @Query("SELECT * FROM questions WHERE classLevel = :classLevel AND subject = :subject LIMIT :limit") suspend fun questions(classLevel: Int, subject: String, limit: Int): List<QuestionEntity>
    @Query("SELECT * FROM questions ORDER BY subject, topic") fun allQuestions(): Flow<List<QuestionEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertSubjects(items: List<SubjectEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertQuestions(items: List<QuestionEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAttempt(item: QuizAttemptEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAnswers(items: List<QuizAnswerEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertWeakTopic(item: WeakTopicEntity)
    @Query("SELECT * FROM quiz_attempts ORDER BY createdAt DESC LIMIT 10") fun recentAttempts(): Flow<List<QuizAttemptEntity>>
    @Query("SELECT * FROM weak_topics ORDER BY misses DESC LIMIT 6") fun weakTopics(): Flow<List<WeakTopicEntity>>
}

@Dao
interface ContentDao {
    @Query("SELECT * FROM content_manifest ORDER BY classLevel, subject, title") fun manifest(): Flow<List<ContentManifestEntity>>
    @Query("SELECT * FROM content_manifest WHERE type = 'past_paper' ORDER BY year DESC") fun pastPaperManifest(): Flow<List<ContentManifestEntity>>
    @Query("SELECT * FROM downloaded_files") fun downloads(): Flow<List<DownloadedFileEntity>>
    @Query("SELECT * FROM downloaded_files WHERE manifestId = :id") suspend fun download(id: String): DownloadedFileEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertManifest(items: List<ContentManifestEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertDownload(item: DownloadedFileEntity)
    @Query("DELETE FROM downloaded_files WHERE manifestId = :id") suspend fun deleteDownload(id: String)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertPastPapers(items: List<PastPaperEntity>)
}

@Dao
interface NotesDao {
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC") fun notes(): Flow<List<NoteEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertNote(item: NoteEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveAiAnswer(item: SavedAiAnswerEntity)
    @Query("SELECT * FROM saved_ai_answers ORDER BY createdAt DESC") fun savedAnswers(): Flow<List<SavedAiAnswerEntity>>
}

@Dao
interface StudyPlanDao {
    @Query("SELECT * FROM study_plans ORDER BY createdAt DESC LIMIT 1") fun activePlan(): Flow<StudyPlanEntity?>
    @Query("SELECT * FROM study_tasks ORDER BY dueDate ASC") fun tasks(): Flow<List<StudyTaskEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertPlan(item: StudyPlanEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertTasks(items: List<StudyTaskEntity>)
    @Query("UPDATE study_tasks SET done = :done WHERE id = :id") suspend fun setTaskDone(id: String, done: Boolean)
}

@Dao
interface AiProviderDao {
    @Query("SELECT * FROM ai_provider_configs ORDER BY priority ASC") fun providers(): Flow<List<AiProviderConfigEntity>>
    @Query("SELECT * FROM ai_provider_configs WHERE id = :id") suspend fun provider(id: String): AiProviderConfigEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertProvider(item: AiProviderConfigEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertProviders(items: List<AiProviderConfigEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertLog(item: ProviderLogEntity)
    @Query("DELETE FROM ai_provider_configs") suspend fun clearProviders()
}
