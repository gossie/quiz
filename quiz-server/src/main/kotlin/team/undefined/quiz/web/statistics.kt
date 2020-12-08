package team.undefined.quiz.web

import team.undefined.quiz.core.AnswerCommand

data class QuizStatisticsDTO(
    val questionStatistics: List<QuestionStatisticsDTO> = ArrayList()
)

data class QuestionStatisticsDTO(
    val question: QuestionDTO,
    val answerStatistics: List<AnswerStatisticsDTO> = ArrayList()
)

data class AnswerStatisticsDTO(
    val participant: ParticipantDTO,
    val duration: Long,
    val answer: String? = null,
    val rating: AnswerCommand.Answer
)
