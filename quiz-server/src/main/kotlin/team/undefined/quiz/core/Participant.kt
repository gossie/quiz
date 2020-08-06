package team.undefined.quiz.core

import java.util.UUID

data class Participant(
        val id: UUID = UUID.randomUUID(),
        val name: String,
        var turn: Boolean = false,
        var points: Long = 0
)
