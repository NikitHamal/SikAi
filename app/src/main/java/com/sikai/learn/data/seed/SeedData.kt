package com.sikai.learn.data.seed

import com.sikai.learn.data.db.entity.PastPaperEntity
import com.sikai.learn.data.db.entity.QuestionEntity
import com.sikai.learn.data.db.entity.SubjectEntity

object SeedData {

    fun subjects(): List<SubjectEntity> {
        val classes = listOf(8, 10, 12)
        val cls8 = listOf("English", "Nepali", "Mathematics", "Science", "Social Studies", "Computer Science")
        val cls10 = listOf("English", "Nepali", "Mathematics", "Science", "Social Studies", "Account", "Computer Science")
        val cls12 = listOf("English", "Nepali", "Mathematics", "Physics", "Chemistry", "Biology", "Account", "Economics", "Computer Science")

        val out = mutableListOf<SubjectEntity>()
        classes.forEach { c ->
            val list = when (c) { 8 -> cls8; 10 -> cls10; 12 -> cls12; else -> emptyList() }
            list.forEachIndexed { idx, name ->
                out.add(SubjectEntity(
                    id = "$c-${name.lowercase().replace(' ', '_')}",
                    name = name,
                    classLevel = c,
                    symbol = name.first().toString(),
                    accentColor = "#C5A059",
                    orderIndex = idx,
                ))
            }
        }
        return out
    }

    /** A small bundled question bank so quizzes work fully offline on first install. */
    fun seedQuestions(): List<QuestionEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            QuestionEntity(
                id = "q-10-mat-1",
                classLevel = 10, subject = "Mathematics", topic = "Algebra",
                prompt = "If 2x + 3 = 11, what is x?",
                optionsJson = """["3","4","5","6"]""",
                correctIndex = 1,
                explanation = "Subtract 3 from both sides: 2x = 8, then x = 4.",
                difficulty = 1, source = "seed", createdAt = now,
            ),
            QuestionEntity(
                id = "q-10-mat-2",
                classLevel = 10, subject = "Mathematics", topic = "Geometry",
                prompt = "The sum of interior angles of a triangle is:",
                optionsJson = """["90°","180°","270°","360°"]""",
                correctIndex = 1,
                explanation = "All triangles have interior angles summing to 180°.",
                difficulty = 1, source = "seed", createdAt = now,
            ),
            QuestionEntity(
                id = "q-10-sci-1",
                classLevel = 10, subject = "Science", topic = "Physics",
                prompt = "SI unit of force is:",
                optionsJson = """["Joule","Pascal","Newton","Watt"]""",
                correctIndex = 2,
                explanation = "Force is measured in Newtons (N), where 1 N = 1 kg·m/s².",
                difficulty = 1, source = "seed", createdAt = now,
            ),
            QuestionEntity(
                id = "q-10-sci-2",
                classLevel = 10, subject = "Science", topic = "Chemistry",
                prompt = "Chemical formula for table salt is:",
                optionsJson = """["KCl","NaCl","CaCO3","HCl"]""",
                correctIndex = 1,
                explanation = "Table salt is sodium chloride: NaCl.",
                difficulty = 1, source = "seed", createdAt = now,
            ),
            QuestionEntity(
                id = "q-10-eng-1",
                classLevel = 10, subject = "English", topic = "Grammar",
                prompt = "Choose the correct sentence:",
                optionsJson = """["He don't like tea","He doesn't likes tea","He doesn't like tea","He not like tea"]""",
                correctIndex = 2,
                explanation = "With 'doesn't' (does not), use the base form: like (not likes).",
                difficulty = 1, source = "seed", createdAt = now,
            ),
            QuestionEntity(
                id = "q-12-phy-1",
                classLevel = 12, subject = "Physics", topic = "Mechanics",
                prompt = "A body of mass 2 kg accelerates at 5 m/s². The force on it is:",
                optionsJson = """["2.5 N","7 N","10 N","25 N"]""",
                correctIndex = 2,
                explanation = "F = m × a = 2 × 5 = 10 N.",
                difficulty = 2, source = "seed", createdAt = now,
            ),
            QuestionEntity(
                id = "q-12-bio-1",
                classLevel = 12, subject = "Biology", topic = "Genetics",
                prompt = "DNA stands for:",
                optionsJson = """["Deoxyribonucleic Acid","Dinucleic Acid","Diaminonucleic Acid","Diribonucleic Acid"]""",
                correctIndex = 0,
                explanation = "DNA = Deoxyribonucleic Acid.",
                difficulty = 1, source = "seed", createdAt = now,
            ),
            QuestionEntity(
                id = "q-8-eng-1",
                classLevel = 8, subject = "English", topic = "Vocabulary",
                prompt = "Antonym of 'ancient' is:",
                optionsJson = """["Old","Modern","Fragile","Slow"]""",
                correctIndex = 1,
                explanation = "Modern is the opposite of ancient.",
                difficulty = 1, source = "seed", createdAt = now,
            ),
        )
    }

    fun seedPastPapers(): List<PastPaperEntity> {
        return listOf(
            PastPaperEntity("pp-10-math-2080", "SEE Mathematics 2080", 10, "Mathematics", 2080, "SEE"),
            PastPaperEntity("pp-10-sci-2080", "SEE Science 2080", 10, "Science", 2080, "SEE"),
            PastPaperEntity("pp-10-eng-2080", "SEE English 2080", 10, "English", 2080, "SEE"),
            PastPaperEntity("pp-12-phy-2080", "NEB Physics 2080", 12, "Physics", 2080, "NEB"),
            PastPaperEntity("pp-12-chem-2080", "NEB Chemistry 2080", 12, "Chemistry", 2080, "NEB"),
            PastPaperEntity("pp-12-bio-2080", "NEB Biology 2080", 12, "Biology", 2080, "NEB"),
        )
    }
}
