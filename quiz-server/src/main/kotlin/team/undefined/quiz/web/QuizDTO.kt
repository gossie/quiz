package team.undefined.quiz.web

import org.springframework.hateoas.RepresentationModel

data class QuizDTO(var id: Long? = null, var name: String, var participants: List<String> = emptyList(), var questions: List<String> = emptyList(), var turn: String? = null) : RepresentationModel<QuizDTO>()
