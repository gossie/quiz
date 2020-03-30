package team.undefined.quiz.web

import org.springframework.hateoas.RepresentationModel

data class QuizDTO(var id: Long? = null, var name: String, var participants: List<ParticipantDTO> = emptyList(), var questions: List<String> = emptyList()) : RepresentationModel<QuizDTO>()
