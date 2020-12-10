package team.undefined.quiz.core

import java.util.*
import kotlin.collections.ArrayList

data class QuizStatistics(val questionStatistics: List<QuestionStatistics> = ArrayList()) {

    fun addQuestionStatistic(questionStatistic: QuestionStatistics): QuizStatistics {
        val newQuestionStatistics = ArrayList(questionStatistics)
        newQuestionStatistics.add(questionStatistic)
        return QuizStatistics(newQuestionStatistics)
    }

}

data class QuestionStatistics(val questionId: UUID, val timestamp: Long, val answerStatistics: List<AnswerStatistics> = ArrayList()) {

    fun addAnswerStatistics(answerStatistic: AnswerStatistics) {
        (answerStatistics as MutableList).add(answerStatistic)
        answerStatistics.sortBy { it.duration }
    }

}

data class AnswerStatistics(
        val participantId: UUID,
        val duration: Long,
        val answer: String? = null,
        val choiceId: UUID? = null,
        var rating: AnswerCommand.Answer = AnswerCommand.Answer.INCORRECT
)