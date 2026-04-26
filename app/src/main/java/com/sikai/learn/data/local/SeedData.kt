package com.sikai.learn.data.local

import com.sikai.learn.BuildConfig
import com.sikai.learn.domain.ai.*

object SeedData {
    val subjects = listOf(
        SubjectEntity("english", "English", "8|10|12"),
        SubjectEntity("nepali", "Nepali", "8|10|12"),
        SubjectEntity("mathematics", "Mathematics", "8|10|12"),
        SubjectEntity("science", "Science", "8|10"),
        SubjectEntity("social", "Social Studies", "8|10"),
        SubjectEntity("account", "Account", "12"),
        SubjectEntity("economics", "Economics", "12"),
        SubjectEntity("physics", "Physics", "12"),
        SubjectEntity("chemistry", "Chemistry", "12"),
        SubjectEntity("biology", "Biology", "12"),
        SubjectEntity("computer", "Computer Science", "8|10|12")
    )

    val questions = listOf(
        QuestionEntity("math-10-linear-1", 10, "Mathematics", "Algebra", "If 2x + 5 = 17, what is x?", "4|5|6|7", 2, "Subtract 5 from both sides: 2x = 12. Divide by 2: x = 6."),
        QuestionEntity("science-10-force-1", 10, "Science", "Force", "Which SI unit is used for force?", "Joule|Newton|Watt|Pascal", 1, "Force is measured in Newton (N)."),
        QuestionEntity("english-10-grammar-1", 10, "English", "Grammar", "Choose the correct sentence.", "She go to school.|She goes to school.|She going school.|She gone to school.", 1, "For third person singular in present simple, use goes."),
        QuestionEntity("physics-12-electricity-1", 12, "Physics", "Electricity", "Ohm's law relates voltage to current and what?", "Mass|Resistance|Frequency|Charge", 1, "Ohm's law is V = IR, where R is resistance."),
        QuestionEntity("math-8-percent-1", 8, "Mathematics", "Percentage", "What is 15% of 200?", "15|20|30|45", 2, "15/100 × 200 = 30.")
    )

    val providers = listOf(
        AiProviderConfig("qwen", "Qwen", AiProviderType.QWEN, BuildConfig.QWEN_BASE_URL, "qwen", "qwen-plus", "qwen-vl-plus", setOf(AiCapability.TEXT, AiCapability.VISION, AiCapability.FILE_UPLOAD, AiCapability.STREAMING), 0, true, AiRequestFormat.OPENAI_COMPATIBLE, supportsSearch = true, supportsThinking = true),
        AiProviderConfig("deepinfra", "DeepInfra", AiProviderType.DEEPINFRA, BuildConfig.DEEPINFRA_BASE_URL, "deepinfra", "meta-llama/Meta-Llama-3.1-8B-Instruct", "Qwen/Qwen2-VL-7B-Instruct", setOf(AiCapability.TEXT, AiCapability.VISION), 1),
        AiProviderConfig("gemini", "Gemini", AiProviderType.GEMINI, BuildConfig.GEMINI_BASE_URL, "gemini", "gemini-1.5-flash", "gemini-1.5-flash", setOf(AiCapability.TEXT, AiCapability.VISION, AiCapability.PDF), 2, true, AiRequestFormat.GEMINI_COMPATIBLE),
        AiProviderConfig("openrouter", "OpenRouter", AiProviderType.OPENROUTER, BuildConfig.OPENROUTER_BASE_URL, "openrouter", "openai/gpt-4o-mini", "qwen/qwen-2-vl-72b-instruct", setOf(AiCapability.TEXT, AiCapability.VISION), 3),
        AiProviderConfig("nvidia", "NVIDIA NIM", AiProviderType.NVIDIA, BuildConfig.NVIDIA_BASE_URL, "nvidia", "meta/llama-3.1-8b-instruct", "microsoft/phi-3.5-vision-instruct", setOf(AiCapability.TEXT, AiCapability.VISION), 4),
        AiProviderConfig("deepseek", "DeepSeek", AiProviderType.DEEPSEEK, BuildConfig.DEEPSEEK_BASE_URL, "deepseek", "deepseek-chat", "deepseek-chat", setOf(AiCapability.TEXT), 5)
    ).map { it.toEntity() }
}
