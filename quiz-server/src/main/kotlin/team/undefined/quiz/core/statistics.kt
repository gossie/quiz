package team.undefined.quiz.core

import java.util.*
import kotlin.collections.ArrayList

data class QuizStatistics(val questionStatistics: List<QuestionStatistics> = ArrayList())

data class QuestionStatistics(val questionId: UUID, val answerStatistics: List<AnswerStatistics> = ArrayList()) {

    fun addAnswerStatistics(answerStatistic: AnswerStatistics) {
        (answerStatistics as MutableList).add(answerStatistic)
        answerStatistics.sortBy { it.duration }
    }

}

data class AnswerStatistics(val participantId: UUID, val duration: Long, val answer: String = "", val rating: AnswerCommand.Answer = AnswerCommand.Answer.INCORRECT)