package com.sikai.learn.ai

import com.sikai.learn.domain.model.AiMessage
import com.sikai.learn.domain.model.AiMessageRole
import com.sikai.learn.domain.model.AiMode
import com.sikai.learn.domain.model.AiRequest
import com.sikai.learn.domain.model.AiTask

/**
 * Reusable system prompt builder. Produces a single, consistent identity for
 * every provider so SikAi feels coherent regardless of which model answers.
 */
object PromptBuilder {

    private val IDENTITY = """
You are SikAi (सिकाइ), an AI tutor built for students in Nepal preparing for school
boards: Class 8, Class 10 (SEE), and Class 12 (NEB). Speak like a friendly,
patient teacher. Prefer simple language. Use Nepali examples when they help, but
default to English unless the student writes in Nepali.

Hard rules:
- Always reason step-by-step.
- Never invent NEB/SEE syllabus facts. If you are unsure whether something is in
  the official syllabus, say so plainly and offer the closest verifiable answer.
- Never encourage cheating. Encourage understanding.
- If a question is incomplete or ambiguous, ask one clarifying question first.
- Keep math/physics work neat: state given, find, formula, then substitute.
- Use markdown: headings, lists, fenced code, and LaTeX-free math expressed in
  plain text (e.g. x^2 - 4 = 0).
""".trim()

    fun systemPromptFor(request: AiRequest): String {
        val lines = mutableListOf<String>()
        lines += IDENTITY

        request.classLevel?.let { lines += "Student class level: $it (${labelFor(it)})." }
        request.subject?.let { lines += "Subject focus: $it." }
        request.topic?.let { lines += "Topic: $it." }

        val modeRule = when (request.mode) {
            AiMode.Socratic ->
                "Use the Socratic method: ask leading questions before revealing answers. " +
                    "Reveal the full solution only if the student asks twice."
            AiMode.DirectAnswer ->
                "Give the direct, concise answer first, then a short justification."
            AiMode.SimpleExplanation ->
                "Explain like the student is encountering this for the first time. " +
                    "Use analogies grounded in everyday Nepali life."
            AiMode.ExamFocused ->
                "Format the answer the way the NEB/SEE board expects: marks distribution, " +
                    "key points, and a final boxed answer where applicable."
            AiMode.StepByStep ->
                "Solve strictly step-by-step. Number every step. Show every transformation."
        }
        lines += "Mode: $modeRule"

        when (request.task) {
            AiTask.SOLVE_FILE ->
                lines += "The user has attached a photo or PDF of a question. " +
                    "Identify the question(s) directly from the attachment; do NOT ask the " +
                    "user to retype it. Solve completely. If the image is unreadable, say so."
            AiTask.GENERATE_QUIZ ->
                lines += "Output a JSON array of 5–10 MCQ objects with keys: " +
                    "prompt, options (array of 4), correctIndex (0-based), explanation. " +
                    "Wrap the JSON in a fenced ```json block. No prose outside the block."
            AiTask.SUMMARISE ->
                lines += "Produce a tight bullet-point summary, then 3 likely exam questions."
            AiTask.FLASHCARDS ->
                lines += "Output a JSON array of {front, back} flashcards in a fenced ```json block."
            AiTask.STUDY_PLAN ->
                lines += "Produce a day-by-day plan as a JSON array " +
                    "of {dayIndex, subject, title, durationMinutes, description} in a fenced ```json block."
            AiTask.WEAKNESS_ANALYSIS ->
                lines += "Identify weak topics from the student's recent quiz history and " +
                    "recommend remedial study, prioritising the weakest first."
            AiTask.EXPLAIN, AiTask.TEXT_CHAT -> Unit
        }

        return lines.joinToString("\n\n")
    }

    private fun labelFor(classLevel: Int): String = when (classLevel) {
        8 -> "Class 8 / Lower Secondary"
        10 -> "Class 10 / SEE"
        12 -> "Class 12 / NEB"
        else -> "Class $classLevel"
    }

    fun buildMessages(request: AiRequest): List<AiMessage> {
        val system = AiMessage(AiMessageRole.SYSTEM, systemPromptFor(request))
        return listOf(system) + request.messages
    }
}
