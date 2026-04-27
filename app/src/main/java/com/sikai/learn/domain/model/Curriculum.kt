package com.sikai.learn.domain.model

/**
 * Static curriculum reference for Nepali school boards. This is the canonical
 * list of subjects the app surfaces during onboarding. It is NOT meant to be a
 * full syllabus database — that comes from the downloadable backend manifest.
 */
object NepalCurriculum {
    val classes = listOf(8, 10, 12)

    val subjectsByClass: Map<Int, List<Subject>> = mapOf(
        8 to listOf(
            Subject("english_8", "English", listOf(8)),
            Subject("nepali_8", "Nepali", listOf(8)),
            Subject("math_8", "Mathematics", listOf(8)),
            Subject("science_8", "Science", listOf(8)),
            Subject("social_8", "Social Studies", listOf(8)),
            Subject("computer_8", "Computer Science", listOf(8)),
            Subject("optmath_8", "Optional Math", listOf(8)),
        ),
        10 to listOf(
            Subject("english_10", "English (SEE)", listOf(10)),
            Subject("nepali_10", "Nepali (SEE)", listOf(10)),
            Subject("math_10", "Mathematics (SEE)", listOf(10)),
            Subject("science_10", "Science (SEE)", listOf(10)),
            Subject("social_10", "Social Studies (SEE)", listOf(10)),
            Subject("optmath_10", "Optional Math (SEE)", listOf(10)),
            Subject("computer_10", "Computer Science (SEE)", listOf(10)),
            Subject("account_10", "Accountancy (SEE)", listOf(10)),
        ),
        12 to listOf(
            Subject("english_12", "English (NEB)", listOf(12)),
            Subject("nepali_12", "Nepali (NEB)", listOf(12)),
            Subject("physics_12", "Physics (NEB)", listOf(12)),
            Subject("chemistry_12", "Chemistry (NEB)", listOf(12)),
            Subject("math_12", "Mathematics (NEB)", listOf(12)),
            Subject("biology_12", "Biology (NEB)", listOf(12)),
            Subject("computer_12", "Computer Science (NEB)", listOf(12)),
            Subject("economics_12", "Economics (NEB)", listOf(12)),
            Subject("account_12", "Accountancy (NEB)", listOf(12)),
        ),
    )

    fun subjectsFor(classLevel: Int): List<Subject> =
        subjectsByClass[classLevel] ?: emptyList()
}
