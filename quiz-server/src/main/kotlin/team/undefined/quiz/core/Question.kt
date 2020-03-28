package team.undefined.quiz.core

data class Question(val id: Long? = null, val question: String, var pending: Boolean = false)
