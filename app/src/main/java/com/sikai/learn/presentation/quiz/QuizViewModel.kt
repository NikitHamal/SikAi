package com.sikai.learn.presentation.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.data.local.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class QuizUiState(val questions: List<QuestionEntity> = emptyList(), val index: Int = 0, val selected: Map<String, Int> = emptyMap(), val completed: Boolean = false, val correct: Int = 0)

@HiltViewModel
class QuizViewModel @Inject constructor(private val dao: LearningDao) : ViewModel() {
    var state = androidx.compose.runtime.mutableStateOf(QuizUiState())
        private set
    fun load(classLevel: Int, subject: String) = viewModelScope.launch { state.value = QuizUiState(questions = dao.questions(classLevel, subject, 10)) }
    fun answer(question: QuestionEntity, selected: Int) { state.value = state.value.copy(selected = state.value.selected + (question.id to selected)) }
    fun nextOrFinish(classLevel: Int, subject: String) = viewModelScope.launch {
        val s = state.value
        if (s.index < s.questions.lastIndex) state.value = s.copy(index = s.index + 1) else {
            val correct = s.questions.count { s.selected[it.id] == it.answerIndex }
            val id = UUID.randomUUID().toString()
            dao.insertAttempt(QuizAttemptEntity(id, classLevel, subject, "Mixed", correct, s.questions.size, System.currentTimeMillis()))
            dao.insertAnswers(s.questions.map { QuizAnswerEntity(id, it.id, s.selected[it.id] ?: -1, s.selected[it.id] == it.answerIndex) })
            s.questions.filter { s.selected[it.id] != it.answerIndex }.forEach { dao.upsertWeakTopic(WeakTopicEntity(classLevel, it.subject, it.topic, 1, System.currentTimeMillis())) }
            state.value = s.copy(completed = true, correct = correct)
        }
    }
}
