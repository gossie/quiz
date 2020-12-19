package team.undefined.quiz.web

import org.springframework.hateoas.RepresentationModel
import java.util.*

data class ChoiceDTO(
        var id: UUID? = null,
        var choice: String
) : RepresentationModel<ChoiceDTO>()