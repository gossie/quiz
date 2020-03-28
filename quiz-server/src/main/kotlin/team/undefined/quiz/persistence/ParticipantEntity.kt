package team.undefined.quiz.persistence

import org.springframework.data.annotation.Id

data class ParticipantEntity(@Id var id: Long?, var name: String, var turn: Int, var quizId: Long)