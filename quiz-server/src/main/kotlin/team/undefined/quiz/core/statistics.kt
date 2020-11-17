package team.undefined.quiz.core

import java.util.*
import kotlin.collections.ArrayList

data class QuizStatistics(val questionStatistics: List<QuestionStatistics> = ArrayList())

data class QuestionStatistics(val questionId: UUID, val buzzerStatistics: List<BuzzerStatistics> = ArrayList()) {

    fun addBuzzerStatistics(buzzerStatistic: BuzzerStatistics) {
        (buzzerStatistics as MutableList).add(buzzerStatistic)
        buzzerStatistics.sortBy { it.duration }
    }

}

data class BuzzerStatistics(val participantId: UUID, val duration: Long, val answer: AnswerCommand.Answer)