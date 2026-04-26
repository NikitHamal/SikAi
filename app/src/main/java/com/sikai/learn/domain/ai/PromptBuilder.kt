package com.sikai.learn.domain.ai

import javax.inject.Inject

class PromptBuilder @Inject constructor() {
    fun systemPrompt(classLevel: Int, subject: String, mode: AiPromptMode, topic: String? = null): String {
        val exam = if (classLevel == 10) "SEE" else if (classLevel == 12) "NEB" else "Nepal school board"
        val modeInstruction = when (mode) {
            AiPromptMode.SOCRATIC -> "Guide with short Socratic questions before revealing the answer."
            AiPromptMode.DIRECT -> "Give a direct explanation with concise steps."
            AiPromptMode.SIMPLE -> "Use very simple language and everyday Nepali examples when helpful."
            AiPromptMode.EXAM -> "Format the answer for board exam marks, with definitions, steps, and final answer."
            AiPromptMode.SOLVE_STEPS -> "Identify the question from the attachment and solve step by step without OCR pre-processing."
            AiPromptMode.GENERATE_QUIZ -> "Generate MCQs with four options, answer key, and explanations."
            AiPromptMode.SUMMARIZE_NOTE -> "Summarize into revision bullets and formulas."
            AiPromptMode.FLASHCARDS -> "Create compact question-answer flashcards."
            AiPromptMode.STUDY_PLANNER -> "Create an adaptive daily plan based on exam date and availability."
            AiPromptMode.WEAKNESS_ANALYSIS -> "Analyze weak topics and suggest targeted practice."
        }
        return """
            You are SikAi, a calm and trustworthy AI tutor for Nepali students.
            Student context: Class $classLevel, $exam, subject: $subject${topic?.let { ", topic: $it" } ?: ""}.
            Teach for understanding, not cheating. Use simple language, step-by-step reasoning, and exam-oriented structure.
            Never hallucinate syllabus facts; if uncertain, say what is uncertain and how the student can verify it.
            Prefer Nepali context and examples where useful, while keeping the answer readable in English.
            $modeInstruction
        """.trimIndent()
    }
}
