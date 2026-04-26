package com.sikai.learn.ui.screens.progress

import androidx.lifecycle.ViewModel
import com.sikai.learn.core.storage.UserPreferences
import com.sikai.learn.data.db.entity.QuizAttemptEntity
import com.sikai.learn.data.db.entity.WeakTopicEntity
import com.sikai.learn.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    repo: QuizRepository,
    val prefs: UserPreferences,
) : ViewModel() {

    val attempts: Flow<List<QuizAttemptEntity>> = repo.observeRecentAttempts()
    val weakTopics: Flow<List<WeakTopicEntity>> = repo.observeWeakTopics()
    val streak: Flow<Int> = prefs.streakDays
    val xp: Flow<Int> = prefs.xp
}
