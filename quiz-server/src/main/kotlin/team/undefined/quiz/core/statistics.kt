package team.undefined.quiz.core

import java.util.*
import kotlin.collections.ArrayList

data class QuizStatistics(
        val participantStatistics: List<ParticipantStatistics> = ArrayList()
) {

    fun addParticipantStatistics(participantStatistic: ParticipantStatistics): QuizStatistics {
        val newParticipantStatistic = ArrayList(participantStatistics)
        newParticipantStatistic.add(participantStatistic)
        return QuizStatistics(newParticipantStatistic)
    }

    fun addQuestionStatistic(questionStatistic: QuestionStatistics): QuizStatistics {
        val newParticipantStatistics = participantStatistics.map { it.addQuestionStatistic(questionStatistic) }
        return QuizStatistics(newParticipantStatistics)
    }

    fun deleteParticipantStatistics(participantId: UUID): QuizStatistics {
        val newParticipantStatistics = participantStatistics.filter { it.id != participantId }
        return QuizStatistics(newParticipantStatistics)
    }

    fun handleAnswer(participantId: UUID, answer: AnswerCommand.Answer): QuizStatistics {
        val newParticipantStatistics = participantStatistics.map {
            if (it.id == participantId) {
                it.handleRating(answer)
            } else {
                it
            }
        }
        return QuizStatistics(newParticipantStatistics)
    }

}

data class ParticipantStatistics(
        val id: UUID,
        val questionStatistics: List<QuestionStatistics> = ArrayList()
) {
    fun addQuestionStatistic(questionStatistic: QuestionStatistics): ParticipantStatistics {
        val newQuestionStatistics = ArrayList(questionStatistics)
        newQuestionStatistics.add(questionStatistic)
        return ParticipantStatistics(id, newQuestionStatistics)
    }

    fun handleRating(rating: AnswerCommand.Answer): ParticipantStatistics {
        val newQuestionStatistics = questionStatistics.mapIndexed { index, question ->
            if (index == questionStatistics.size - 1) {
                question.addRating(rating)
            } else {
                question
            }
        }
        return ParticipantStatistics(id, newQuestionStatistics)
    }
}

data class QuestionStatistics(
        val id: UUID,
        val ratings: List<AnswerCommand.Answer> = ArrayList()
) {
    fun addRating(rating: AnswerCommand.Answer): QuestionStatistics {
        val newRatings = ArrayList(ratings)
        newRatings.add(rating)
        return QuestionStatistics(id, newRatings)
    }
}
