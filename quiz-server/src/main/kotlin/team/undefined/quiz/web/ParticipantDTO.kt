package team.undefined.quiz.web

import org.springframework.hateoas.RepresentationModel

data class ParticipantDTO(val id: Long? = null, val name: String, val turn: Boolean, val points: Long) : RepresentationModel<ParticipantDTO>()
