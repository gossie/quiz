package team.undefined.quiz.core

import java.util.UUID

data class Question(val id: UUID = UUID.randomUUID(), val question: String, var pending: Boolean = false, val imageUrl: String = "", var alreadyPlayed: Boolean = false)
