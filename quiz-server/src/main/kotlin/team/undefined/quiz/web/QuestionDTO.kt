package team.undefined.quiz.web

import org.springframework.hateoas.RepresentationModel
import java.util.UUID

data class QuestionDTO(var id: UUID? = null, var question: String, var pending: Boolean = true, var imagePath: String = "") : RepresentationModel<QuestionDTO>()
