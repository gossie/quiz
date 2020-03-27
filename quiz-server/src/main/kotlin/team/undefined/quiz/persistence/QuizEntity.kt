package team.undefined.quiz.persistence

import org.springframework.data.annotation.Id

data class QuizEntity(@Id var id: Long?, var name: String, var participants: String, var turn: String?)
