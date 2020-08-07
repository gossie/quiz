package team.undefined.quiz.web

import org.springframework.hateoas.RepresentationModel
import java.util.UUID

data class QuestionDTO(
        var id: UUID? = null,
        var question: String,
        var pending: Boolean = true,
        var imagePath: String = "",
        var estimates: Map<UUID, String>? = null,
        var publicVisible: Boolean = false,
        var category: String = "",
        var timeToAnswer: Int? = null
) : RepresentationModel<QuestionDTO>()
