package team.undefined.quiz.core

import java.util.UUID

data class Participant(
        val id: UUID = UUID.randomUUID(),
        val name: String,
        val turn: Boolean = false,
        val points: Long = 0,
        val revealAllowed: Boolean = true
)
