package com.sikai.learn.presentation.screens.quizzes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.data.repository.QuizRepository
import com.sikai.learn.data.repository.UserProfileRepository
import com.sikai.learn.domain.model.NepalCurriculum
import com.sikai.learn.domain.model.QuizQuestion
import com.sikai.learn.domain.model.Subject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizzesState(
    val classLevel: Int? = null,
    val subjects: List<Subject> = emptyList(),
    val selectedSubjectId: String? = null,
    val loading: Boolean = false,
    val questions: List<QuizQuestion> = emptyList(),
    val selected: Map<String, Int> = emptyMap(),
    val finished: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class QuizzesViewModel @Inject constructor(
    private val users: UserProfileRepository,
    private val quizzes: QuizRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(QuizzesState())
    val state: StateFlow<QuizzesState> = _state.asStateFlow()
    private var startedAtMillis = 0L

    init {
        viewModelScope.launch {
            users.observe().collect { profile ->
                val cls = profile?.classLevel
                val subjects = cls?.let { lvl ->
                    val all = NepalCurriculum.subjectsFor(lvl)
                    if (profile.subjects.isNotEmpty())
                        all.filter { it.id in profile.subjects }
                    else all
                }.orEmpty()
                _state.update {
                    it.copy(classLevel = cls, subjects = subjects, selectedSubjectId = subjects.firstOrNull()?.id)
                }
            }
        }
    }

    fun selectSubject(id: String) {
        _state.update { it.copy(selectedSubjectId = id) }
    }

    fun startQuiz() {
        val cls = _state.value.classLevel ?: return
        viewModelScope.launch {
            _state.update { it.copy(loading = true, errorMessage = null, finished = false, selected = emptyMap()) }
            startedAtMillis = System.currentTimeMillis()
            val subjectId = _state.value.selectedSubjectId
            val qs = runCatching { quizzes.pickQuestions(cls, subjectId) }
                .onFailure { t ->
                    _state.update { current -> current.copy(errorMessage = t.message ?: "AI quiz failed; try again.") }
                }
                .getOrDefault(emptyList())
            _state.update { it.copy(loading = false, questions = qs) }
        }
    }

    fun selectAnswer(questionId: String, index: Int) {
        _state.update { it.copy(selected = it.selected + (questionId to index)) }
    }

    fun submit() {
        val s = _state.value
        if (s.questions.isEmpty() || s.classLevel == null) return
        viewModelScope.launch {
            quizzes.saveAttempt(
                classLevel = s.classLevel,
                subject = s.selectedSubjectId ?: "general",
                topic = null,
                all = s.questions,
                selected = s.selected,
                startedAtMillis = startedAtMillis,
            )
            _state.update { it.copy(finished = true) }
        }
    }

    fun reset() {
        _state.update {
            it.copy(questions = emptyList(), selected = emptyMap(), finished = false, errorMessage = null)
        }
    }
}
