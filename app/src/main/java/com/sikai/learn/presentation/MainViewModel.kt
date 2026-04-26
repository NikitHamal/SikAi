package com.sikai.learn.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.core.security.AppPreferences
import com.sikai.learn.data.local.UserDao
import com.sikai.learn.data.repository.AppStartupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    startup: AppStartupRepository,
    userDao: UserDao,
    prefs: AppPreferences
) : ViewModel() {
    val profile = userDao.profile().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val preferences = prefs.flow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    init { viewModelScope.launch { startup.seedIfNeeded() } }
}
