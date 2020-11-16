package team.undefined.quiz.web

import org.springframework.hateoas.RepresentationModel
import java.util.UUID

data class QuizDTO(
        var id: UUID? = null,
        var name: String,
        var participants: List<ParticipantDTO> = emptyList(),
        var playedQuestions: List<QuestionDTO> = emptyList(),
        var openQuestions: List<QuestionDTO> = emptyList(),
        var undoPossible: Boolean = false,
        var redoPossible: Boolean = false,
        var finished: Boolean = false,
        var quizStatistics: QuizStatisticsDTO? = null,
        var timestamp: Long
) : RepresentationModel<QuizDTO>()
