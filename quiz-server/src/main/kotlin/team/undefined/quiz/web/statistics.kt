package team.undefined.quiz.web

import team.undefined.quiz.core.AnswerCommand

data class QuizStatisticsDTO(val questionStatistics: List<QuestionStatisticsDTO> = ArrayList())

data class QuestionStatisticsDTO(val question: QuestionDTO, val buzzerStatistics: List<BuzzerStatisticsDTO> = ArrayList())

data class BuzzerStatisticsDTO(val participant: ParticipantDTO, val duration: Long, val answer: AnswerCommand.Answer)
