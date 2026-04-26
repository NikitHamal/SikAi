package com.sikai.learn.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.data.local.UserDao
import com.sikai.learn.data.local.UserProfileEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(private val userDao: UserDao) : ViewModel() {
    fun complete(classLevel: Int, language: String, subjects: List<String>, examDate: String?, onDone: () -> Unit) {
        viewModelScope.launch {
            userDao.upsert(UserProfileEntity(classLevel = classLevel, language = language, subjectsCsv = subjects.joinToString(","), examDate = examDate?.takeIf { it.isNotBlank() }, onboardingComplete = true))
            onDone()
        }
    }
}
