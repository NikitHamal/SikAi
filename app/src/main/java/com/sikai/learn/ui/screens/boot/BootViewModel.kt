package com.sikai.learn.ui.screens.boot

import androidx.lifecycle.ViewModel
import com.sikai.learn.core.storage.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class BootViewModel @Inject constructor(
    prefs: UserPreferences,
) : ViewModel() {
    val onboarded: Flow<Boolean> = prefs.onboarded
}
