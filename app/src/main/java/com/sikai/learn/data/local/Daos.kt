package com.sikai.learn.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 0")
    fun observe(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 0")
    suspend fun get(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: UserProfileEntity)

    @Query("UPDATE user_profile SET streakDays = :streak, xp = :xp WHERE id = 0")
    suspend fun updateStreak(streak: Int, xp: Int)
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subject WHERE classLevel = :classLevel")
    fun observeForClass(classLevel: Int): Flow<List<SubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<SubjectEntity>)
}

@Dao
interface ContentManifestDao {
    @Query("SELECT * FROM content_manifest")
    fun observeAll(): Flow<List<ContentManifestEntity>>

    @Query("SELECT * FROM content_manifest WHERE classLevel = :classLevel")
    fun observeForClass(classLevel: Int): Flow<List<ContentManifestEntity>>

    @Query("SELECT * FROM content_manifest WHERE id = :id")
    suspend fun byId(id: String): ContentManifestEntity?

    @Query("SELECT COUNT(*) FROM content_manifest")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ContentManifestEntity>)

    @Query("DELETE FROM content_manifest WHERE id NOT IN (:keepIds)")
    suspend fun deleteAllExcept(keepIds: List<String>)
}

@Dao
interface DownloadedFileDao {
    @Query("SELECT * FROM downloaded_file")
    fun observeAll(): Flow<List<DownloadedFileEntity>>

    @Query("SELECT * FROM downloaded_file WHERE manifestId = :id")
    suspend fun byManifestId(id: String): DownloadedFileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DownloadedFileEntity)

    @Query("DELETE FROM downloaded_file WHERE manifestId = :id")
    suspend fun delete(id: String)
}

@Dao
interface PastPaperDao {
    @Query("SELECT * FROM past_paper WHERE classLevel = :classLevel ORDER BY year DESC")
    fun observeForClass(classLevel: Int): Flow<List<PastPaperEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<PastPaperEntity>)
}

@Dao
interface QuestionDao {
    @Query("SELECT * FROM question WHERE classLevel = :classLevel AND subject = :subject LIMIT :limit")
    suspend fun bySubject(classLevel: Int, subject: String, limit: Int = 20): List<QuestionEntity>

    @Query("SELECT * FROM question WHERE classLevel = :classLevel ORDER BY RANDOM() LIMIT :limit")
    suspend fun random(classLevel: Int, limit: Int = 20): List<QuestionEntity>

    @Query("SELECT COUNT(*) FROM question")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<QuestionEntity>)
}

@Dao
interface QuizAttemptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: QuizAttemptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswers(entities: List<QuizAnswerEntity>)

    @Query("SELECT * FROM quiz_attempt ORDER BY finishedAtMillis DESC LIMIT :limit")
    fun recent(limit: Int = 20): Flow<List<QuizAttemptEntity>>
}

@Dao
interface WeakTopicDao {
    @Query("SELECT * FROM weak_topic WHERE classLevel = :classLevel ORDER BY strengthScore ASC LIMIT :limit")
    fun weakest(classLevel: Int, limit: Int = 5): Flow<List<WeakTopicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WeakTopicEntity)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM note ORDER BY updatedAtMillis DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM note WHERE id = :id")
    suspend fun byId(id: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: NoteEntity)

    @Query("DELETE FROM note WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface SavedAiAnswerDao {
    @Query("SELECT * FROM saved_ai_answer ORDER BY savedAtMillis DESC")
    fun observeAll(): Flow<List<SavedAiAnswerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SavedAiAnswerEntity)
}

@Dao
interface StudyPlanDao {
    @Query("SELECT * FROM study_plan ORDER BY createdAtMillis DESC LIMIT 1")
    fun observeCurrent(): Flow<StudyPlanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: StudyPlanEntity)

    @Query("SELECT * FROM study_task WHERE planId = :planId ORDER BY dayIndex ASC")
    fun tasks(planId: String): Flow<List<StudyTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTasks(tasks: List<StudyTaskEntity>)

    @Update
    suspend fun updateTask(task: StudyTaskEntity)
}

@Dao
interface AiProviderConfigDao {
    @Query("SELECT * FROM ai_provider_config WHERE enabled = 1 ORDER BY priority ASC")
    fun observeEnabled(): Flow<List<AiProviderConfigEntity>>

    @Query("SELECT * FROM ai_provider_config ORDER BY priority ASC")
    fun observeAll(): Flow<List<AiProviderConfigEntity>>

    @Query("SELECT * FROM ai_provider_config WHERE id = :id")
    suspend fun byId(id: String): AiProviderConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AiProviderConfigEntity)

    @Query("DELETE FROM ai_provider_config WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT COUNT(*) FROM ai_provider_config")
    suspend fun count(): Int
}

@Dao
interface ProviderLogDao {
    @Insert
    suspend fun insert(entry: ProviderLogEntity)

    @Query("SELECT * FROM provider_log ORDER BY timestampMillis DESC LIMIT :limit")
    fun recent(limit: Int = 50): Flow<List<ProviderLogEntity>>
}
