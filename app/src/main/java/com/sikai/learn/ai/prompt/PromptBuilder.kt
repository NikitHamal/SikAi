package com.sikai.learn.ai.prompt

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reusable prompt builder for SikAi. Composes the base tutor identity with class/subject
 * context and the active mode (Socratic, direct, exam, quiz, summarize, etc.).
 *
 * The system prompts here are deliberately careful: they tell the model to be
 * exam-aware (NEB / SEE), to use plain language, and to admit uncertainty rather
 * than hallucinate syllabus facts.
 */
@Singleton
class PromptBuilder @Inject constructor() {

    fun base(): String = """
        You are SikAi, an AI tutor built for students in Nepal. You are calm, encouraging, and exam-aware.
        - Always favour plain language. Prefer step-by-step structure.
        - Never invent syllabus facts. If unsure about NEB/SEE coverage, say so plainly.
        - Encourage understanding, not cheating. Show working, not just answers.
        - Use Nepali examples when culturally helpful (price in रू, places in Nepal, etc.).
        - Use Markdown for formatting: headings, lists, fenced code, tables, and LaTeX-like math when needed.
        - Keep responses focused. Avoid filler.
    """.trimIndent()

    fun forContext(classLevel: Int?, subject: String?, topic: String?): String {
        val classLine = classLevel?.let { "Student class: Class $it (${board(it)})." } ?: ""
        val subjectLine = subject?.let { "Subject: $it." } ?: ""
        val topicLine = topic?.let { "Current topic: $it." } ?: ""
        return listOf(classLine, subjectLine, topicLine).filter { it.isNotBlank() }.joinToString(" ")
    }

    private fun board(classLevel: Int): String = when (classLevel) {
        8 -> "Lower Secondary"
        10 -> "SEE board"
        11, 12 -> "NEB board"
        else -> "School"
    }

    fun mode(mode: TutorMode): String = when (mode) {
        TutorMode.Socratic -> "Be Socratic. Ask one focused question at a time, guide the student to the answer. Reveal the answer only after the student is close."
        TutorMode.Direct -> "Give a direct, complete explanation. Use 3–6 short steps."
        TutorMode.SimpleExplain -> "Explain like the student is hearing this for the first time. Use simple words, vivid analogies, and a short example."
        TutorMode.ExamAnswer -> "Format as an exam answer. Lead with definitions, then key points (bulleted), then a worked example. Highlight what carries marks."
        TutorMode.SolveStepByStep -> "Solve step by step. Show every line of working. End with a single boxed final answer."
        TutorMode.GenerateQuiz -> "Generate a multiple-choice quiz. Output JSON only with: { questions: [ { id, prompt, options:[a,b,c,d], correctIndex, explanation } ] }."
        TutorMode.SummarizeNote -> "Summarize the user's note into a 5-bullet revision sheet, then 3 likely exam questions."
        TutorMode.MakeFlashcards -> "Output flashcards as JSON only: { flashcards: [ { front, back } ] }. Keep fronts under 12 words."
        TutorMode.StudyPlan -> "Create a study plan as JSON only: { plan: [ { day, focusTopic, durationMinutes, tasks:[..] } ] }."
        TutorMode.WeaknessAnalysis -> "Analyze recent quiz attempts. List weak topics, suggest 1 micro-drill each, and prioritize by exam weight."
    }

    fun build(
        userText: String,
        mode: TutorMode = TutorMode.Direct,
        classLevel: Int? = null,
        subject: String? = null,
        topic: String? = null,
        nepaliMode: Boolean = false,
    ): String {
        val base = base()
        val context = forContext(classLevel, subject, topic)
        val nepali = if (nepaliMode) "Respond in Nepali (Devanagari) when natural; mix English for technical terms." else ""
        val modeLine = mode(mode)
        return listOf(base, context, nepali, modeLine, "User input:\n$userText")
            .filter { it.isNotBlank() }
            .joinToString("\n\n")
    }
}

enum class TutorMode {
    Socratic,
    Direct,
    SimpleExplain,
    ExamAnswer,
    SolveStepByStep,
    GenerateQuiz,
    SummarizeNote,
    MakeFlashcards,
    StudyPlan,
    WeaknessAnalysis,
}
