package team.undefined.quiz.web

import team.undefined.quiz.core.AnswerCommand
import java.util.*
import kotlin.collections.ArrayList

data class QuizStatisticsDTO(
        val participantStatistics: List<ParticipantStatisticsDTO> = ArrayList()
)

data class ParticipantStatisticsDTO(
        var participant: ParticipantDTO?,
        var questionStatistics: List<QuestionStatisticsDTO> = ArrayList()
)

data class QuestionStatisticsDTO(
        var question: QuestionDTO,
        var ratings: List<AnswerCommand.Answer> = ArrayList()
)

