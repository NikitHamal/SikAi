package com.sikai.learn.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.core.storage.UserPreferences
import com.sikai.learn.data.db.entity.QuestionEntity
import com.sikai.learn.data.repository.QuizRepository
import com.sikai.learn.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class QuizPhase { Setup, Running, Finished }

data class QuizState(
    val phase: QuizPhase = QuizPhase.Setup,
    val classLevel: Int = 10,
    val subjects: List<String> = emptyList(),
    val selectedSubject: String? = null,
    val questions: List<QuestionEntity> = emptyList(),
    val answers: List<Int> = emptyList(),
    val currentIndex: Int = 0,
    val startedAt: Long = 0L,
    val correctCount: Int = 0,
    val totalCount: Int = 0,
)

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repo: QuizRepository,
    private val prefs: UserPreferences,
    private val userRepo: UserProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(QuizState())
    val state: StateFlow<QuizState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    classLevel = prefs.classLevel.first(),
                    subjects = prefs.subjects.first().ifEmpty { listOf("Mathematics", "Science", "English") },
                )
            }
        }
    }

    fun pickSubject(subject: String) = _state.update { it.copy(selectedSubject = subject) }

    fun startQuiz() {
        val subject = _state.value.selectedSubject ?: return
        viewModelScope.launch {
            val questions = repo.startQuiz(_state.value.classLevel, subject, count = 5)
            if (questions.isEmpty()) return@launch
            _state.update {
                it.copy(
                    phase = QuizPhase.Running,
                    questions = questions,
                    answers = List(questions.size) { -1 },
                    currentIndex = 0,
                    startedAt = System.currentTimeMillis(),
                )
            }
        }
    }

    fun answer(index: Int) {
        val s = _state.value
        if (s.phase != QuizPhase.Running) return
        val newAnswers = s.answers.toMutableList().apply { this[s.currentIndex] = index }
        _state.update { it.copy(answers = newAnswers) }
    }

    fun next() {
        val s = _state.value
        if (s.currentIndex < s.questions.lastIndex) {
            _state.update { it.copy(currentIndex = it.currentIndex + 1) }
        } else {
            finish()
        }
    }

    fun parseOptions(q: QuestionEntity) = repo.parseOptions(q)

    private fun finish() {
        val s = _state.value
        viewModelScope.launch {
            val attempt = repo.finishQuiz(
                classLevel = s.classLevel,
                subject = s.selectedSubject ?: "",
                topic = null,
                questions = s.questions,
                answers = s.answers,
                startedAt = s.startedAt,
            )
            userRepo.bumpStreakAndXp(attempt.correctCount * 10)
            _state.update {
                it.copy(
                    phase = QuizPhase.Finished,
                    correctCount = attempt.correctCount,
                    totalCount = attempt.totalQuestions,
                )
            }
        }
    }

    fun restart() = _state.update { QuizState(classLevel = it.classLevel, subjects = it.subjects) }
}
