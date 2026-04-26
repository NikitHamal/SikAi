package com.sikai.learn.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.data.local.*
import com.sikai.learn.domain.content.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val profile: UserProfileEntity? = null,
    val downloads: Int = 0,
    val weakTopics: List<WeakTopicEntity> = emptyList(),
    val recentAttempts: List<QuizAttemptEntity> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    userDao: UserDao,
    learningDao: LearningDao,
    contentDao: ContentDao,
    contentRepository: ContentRepository
) : ViewModel() {
    val state = combine(userDao.profile(), contentDao.downloads(), learningDao.weakTopics(), learningDao.recentAttempts()) { profile, downloads, weak, attempts ->
        HomeUiState(profile, downloads.size, weak, attempts)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
    init { viewModelScope.launch { contentRepository.refreshManifest() } }
}
