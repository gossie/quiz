package team.undefined.quiz.web

import org.springframework.hateoas.RepresentationModel

data class QuestionDTO(var id: Long? = null, var question: String, var imageName: String) : RepresentationModel<QuestionDTO>()
