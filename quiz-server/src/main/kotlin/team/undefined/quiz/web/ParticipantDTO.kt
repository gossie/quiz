package team.undefined.quiz.web

import org.springframework.hateoas.RepresentationModel
import java.util.UUID

data class ParticipantDTO(val id: UUID, val name: String, val turn: Boolean, val points: Long) : RepresentationModel<ParticipantDTO>()
