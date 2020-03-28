package team.undefined.quiz.core

data class Participant(val id: Long? = null, val name: String, var turn: Boolean = false, var points: Long = 0)
