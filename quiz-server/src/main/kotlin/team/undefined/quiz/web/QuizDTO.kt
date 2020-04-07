package team.undefined.quiz.web

import org.springframework.hateoas.RepresentationModel

data class QuizDTO(var id: Long? = null, var name: String, var participants: List<ParticipantDTO> = emptyList(), var playedQuestions: List<QuestionDTO> = emptyList(), var openQuestions: List<QuestionDTO> = emptyList()) : RepresentationModel<QuizDTO>()
