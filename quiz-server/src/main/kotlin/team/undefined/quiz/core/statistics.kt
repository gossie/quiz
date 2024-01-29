package team.undefined.quiz.core

import java.util.*
import kotlin.collections.ArrayList

data class QuizStatistics(
        val participantStatistics: List<ParticipantStatistics> = ArrayList(),
        val sequenceNumber: Long = 0,
) {

    fun setSequenceNumber(sequenceNumber: Long): QuizStatistics {
        return QuizStatistics(participantStatistics, sequenceNumber)
    }

    fun addParticipantStatistics(participantStatistic: ParticipantStatistics): QuizStatistics {
        val newParticipantStatistic = ArrayList(participantStatistics)
        newParticipantStatistic.add(participantStatistic)
        return QuizStatistics(newParticipantStatistic, sequenceNumber)
    }

    fun addQuestionStatistic(questionStatistic: QuestionStatistics): QuizStatistics {
        val newParticipantStatistics = participantStatistics.map { it.addQuestionStatistic(questionStatistic) }
        return QuizStatistics(newParticipantStatistics, sequenceNumber)
    }

    fun deleteParticipantStatistics(participantId: UUID): QuizStatistics {
        val newParticipantStatistics = participantStatistics.filter { it.participantId != participantId }
        return QuizStatistics(newParticipantStatistics, sequenceNumber)
    }

    fun handleAnswer(participantId: UUID, answer: AnswerCommand.Answer): QuizStatistics {
        val newParticipantStatistics = participantStatistics.map {
            if (it.participantId == participantId) {
                it.handleRating(answer)
            } else {
                it
            }
        }
        return QuizStatistics(newParticipantStatistics, sequenceNumber)
    }

}

data class ParticipantStatistics(
        val participantId: UUID,
        val questionStatistics: List<QuestionStatistics> = ArrayList()
) {
    fun addQuestionStatistic(questionStatistic: QuestionStatistics): ParticipantStatistics {
        val newQuestionStatistics = ArrayList(questionStatistics)
        newQuestionStatistics.add(questionStatistic)
        return ParticipantStatistics(participantId, newQuestionStatistics)
    }

    fun handleRating(rating: AnswerCommand.Answer): ParticipantStatistics {
        val newQuestionStatistics = questionStatistics.mapIndexed { index, question ->
            if (index == questionStatistics.size - 1) {
                question.addRating(rating)
            } else {
                question
            }
        }
        return ParticipantStatistics(participantId, newQuestionStatistics)
    }
}

data class QuestionStatistics(
        val questionId: UUID,
        val ratings: List<AnswerCommand.Answer> = ArrayList()
) {
    fun addRating(rating: AnswerCommand.Answer): QuestionStatistics {
        val newRatings = ArrayList(ratings)
        newRatings.add(rating)
        return QuestionStatistics(questionId, newRatings)
    }
}
