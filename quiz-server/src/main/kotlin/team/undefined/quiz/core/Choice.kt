package team.undefined.quiz.core

import java.util.*

data class Choice(
        val id: UUID = UUID.randomUUID(),
        val choice: String
)
