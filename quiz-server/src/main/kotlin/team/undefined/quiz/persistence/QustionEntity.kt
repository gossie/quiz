package team.undefined.quiz.persistence

import org.springframework.data.annotation.Id

data class QustionEntity(@Id var id: Long?, var question: String, var pending: Int, var quizId: Long)