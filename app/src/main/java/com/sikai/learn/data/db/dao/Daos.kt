package com.sikai.learn.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.sikai.learn.data.db.entity.AiProviderConfigEntity
import com.sikai.learn.data.db.entity.ContentManifestEntity
import com.sikai.learn.data.db.entity.DownloadedFileEntity
import com.sikai.learn.data.db.entity.NoteEntity
import com.sikai.learn.data.db.entity.PastPaperEntity
import com.sikai.learn.data.db.entity.ProviderLogEntity
import com.sikai.learn.data.db.entity.QuestionEntity
import com.sikai.learn.data.db.entity.QuizAnswerEntity
import com.sikai.learn.data.db.entity.QuizAttemptEntity
import com.sikai.learn.data.db.entity.SavedAiAnswerEntity
import com.sikai.learn.data.db.entity.StudyPlanEntity
import com.sikai.learn.data.db.entity.StudyTaskEntity
import com.sikai.learn.data.db.entity.SubjectEntity
import com.sikai.learn.data.db.entity.UserProfileEntity
import com.sikai.learn.data.db.entity.WeakTopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id='me'")
    fun observe(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id='me'")
    suspend fun get(): UserProfileEntity?

    @Upsert
    suspend fun upsert(entity: UserProfileEntity)
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects WHERE classLevel=:classLevel ORDER BY orderIndex")
    fun observeForClass(classLevel: Int): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects ORDER BY classLevel, orderIndex")
    suspend fun all(): List<SubjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<SubjectEntity>)
}

@Dao
interface ContentManifestDao {
    @Query("SELECT * FROM content_manifest ORDER BY classLevel, subject, type")
    fun observeAll(): Flow<List<ContentManifestEntity>>

    @Query("SELECT * FROM content_manifest WHERE classLevel=:classLevel ORDER BY subject")
    fun observeForClass(classLevel: Int): Flow<List<ContentManifestEntity>>

    @Query("SELECT * FROM content_manifest WHERE id=:id")
    suspend fun byId(id: String): ContentManifestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ContentManifestEntity>)

    @Query("DELETE FROM content_manifest WHERE id=:id")
    suspend fun delete(id: String)
}

@Dao
interface DownloadedFileDao {
    @Query("SELECT * FROM downloaded_files")
    fun observeAll(): Flow<List<DownloadedFileEntity>>

    @Query("SELECT * FROM downloaded_files WHERE manifestId=:manifestId")
    suspend fun forManifest(manifestId: String): DownloadedFileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DownloadedFileEntity)

    @Query("DELETE FROM downloaded_files WHERE manifestId=:manifestId")
    suspend fun delete(manifestId: String)
}

@Dao
interface PastPaperDao {
    @Query("SELECT * FROM past_papers WHERE classLevel=:classLevel ORDER BY year DESC, subject")
    fun observeForClass(classLevel: Int): Flow<List<PastPaperEntity>>

    @Query("SELECT * FROM past_papers ORDER BY year DESC, subject")
    fun observeAll(): Flow<List<PastPaperEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<PastPaperEntity>)
}

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE classLevel=:classLevel AND subject=:subject ORDER BY RANDOM() LIMIT :limit")
    suspend fun random(classLevel: Int, subject: String, limit: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE id=:id")
    suspend fun byId(id: String): QuestionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuestionEntity>)

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun count(): Int
}

@Dao
interface QuizAttemptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attempt: QuizAttemptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswers(answers: List<QuizAnswerEntity>)

    @Query("SELECT * FROM quiz_attempts ORDER BY startedAt DESC LIMIT :limit")
    fun recent(limit: Int): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE id=:id")
    suspend fun byId(id: String): QuizAttemptEntity?

    @Query("SELECT * FROM quiz_answers WHERE attemptId=:attemptId")
    suspend fun answersFor(attemptId: String): List<QuizAnswerEntity>
}

@Dao
interface WeakTopicDao {
    @Query("SELECT * FROM weak_topics ORDER BY score ASC LIMIT :limit")
    fun observeWeakest(limit: Int): Flow<List<WeakTopicEntity>>

    @Upsert
    suspend fun upsert(entity: WeakTopicEntity)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id=:id")
    suspend fun byId(id: String): NoteEntity?

    @Upsert
    suspend fun upsert(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id=:id")
    suspend fun delete(id: String)
}

@Dao
interface SavedAiAnswerDao {
    @Query("SELECT * FROM saved_ai_answers ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SavedAiAnswerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SavedAiAnswerEntity)

    @Query("DELETE FROM saved_ai_answers WHERE id=:id")
    suspend fun delete(id: String)
}

@Dao
interface StudyPlanDao {
    @Query("SELECT * FROM study_plans WHERE active=1 ORDER BY createdAt DESC LIMIT 1")
    fun observeActive(): Flow<StudyPlanEntity?>

    @Upsert
    suspend fun upsert(plan: StudyPlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<StudyTaskEntity>)

    @Query("SELECT * FROM study_tasks WHERE planId=:planId ORDER BY scheduledAt ASC")
    fun observeTasks(planId: String): Flow<List<StudyTaskEntity>>

    @Update
    suspend fun updateTask(task: StudyTaskEntity)
}

@Dao
interface AiProviderConfigDao {
    @Query("SELECT * FROM ai_provider_configs ORDER BY priority ASC")
    fun observeAll(): Flow<List<AiProviderConfigEntity>>

    @Query("SELECT * FROM ai_provider_configs ORDER BY priority ASC")
    suspend fun all(): List<AiProviderConfigEntity>

    @Query("SELECT * FROM ai_provider_configs WHERE id=:id")
    suspend fun byId(id: String): AiProviderConfigEntity?

    @Upsert
    suspend fun upsert(entity: AiProviderConfigEntity)

    @Query("DELETE FROM ai_provider_configs WHERE id=:id")
    suspend fun delete(id: String)
}

@Dao
interface ProviderLogDao {
    @Query("SELECT * FROM provider_logs ORDER BY createdAt DESC LIMIT 200")
    fun observeRecent(): Flow<List<ProviderLogEntity>>

    @Insert
    suspend fun insert(entity: ProviderLogEntity)

    @Query("DELETE FROM provider_logs WHERE createdAt < :cutoff")
    suspend fun trim(cutoff: Long)
}
