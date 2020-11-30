package team.undefined.quiz.web

import org.springframework.hateoas.RepresentationModel

data class ChoiceDTO(
        var choice: String
) : RepresentationModel<ChoiceDTO>()