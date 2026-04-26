package com.sikai.learn.ui.screens.papers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.core.storage.UserPreferences
import com.sikai.learn.data.db.entity.PastPaperEntity
import com.sikai.learn.data.repository.PastPaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PastPapersViewModel @Inject constructor(
    repo: PastPaperRepository,
    prefs: UserPreferences,
) : ViewModel() {
    val papers: Flow<List<PastPaperEntity>> =
        prefs.classLevel.flatMapLatest { repo.observeForClass(it) }
}
