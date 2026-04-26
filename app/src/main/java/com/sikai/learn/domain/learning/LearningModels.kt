package com.sikai.learn.domain.learning

data class Subject(val name: String, val classLevels: Set<Int>)
data class Question(val id: String, val classLevel: Int, val subject: String, val topic: String, val prompt: String, val options: List<String>, val answerIndex: Int, val explanation: String)
data class QuizResult(val correct: Int, val total: Int, val xp: Int)
