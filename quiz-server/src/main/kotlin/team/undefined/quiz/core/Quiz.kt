package team.undefined.quiz.core

import java.util.*
import kotlin.collections.ArrayList

data class Quiz(
        val id: UUID = UUID.randomUUID(),
        val name: String,
        val participants: List<Participant> = ArrayList(),
        val questions: List<Question> = ArrayList(),
        var finished: Boolean = false,
        var quizStatistics: QuizStatistics? = null) {

    private var timestamp: Long = Date().time

    val pendingQuestion: Question?
        get() = questions.filter { it.pending }.firstOrNull()

    fun nobodyHasBuzzered(): Boolean {
        return participants
                .none { it.turn }
    }

    fun decreaseTimeToAnswer(questionId: UUID): Quiz {
        val question = questions.find { it.id == questionId }!!
        question.secondsLeft = question.secondsLeft!! - 1
        return this
    }

    fun select(participantId: UUID): Quiz {
        participants
                .find { it.id == participantId }
                ?.turn = true
        return this
    }

    fun estimate(participantId: UUID, estimatedValue: String): Quiz {
        questions
                .find { it.pending }
                ?.estimate(participantId, estimatedValue)
        return this
    }

    fun hasNoParticipantWithName(name: String): Boolean {
        return participants.none { it.name == name }
    }

    fun addParticipantIfNecessary(participant: Participant): Quiz {
        if (hasNoParticipantWithName(participant.name)) {
            (participants as MutableList).add(participant)
        }
        return this
    }

    fun addQuestion(question: Question): Quiz {
        (questions as MutableList).add(question)
        return this
    }

    fun editQuestion(question: Question): Quiz {
        (questions as MutableList).replaceAll {
            if (it.id == question.id) {
                question
            } else {
                it
            }
        }
        return this
    }

    fun deleteQuestion(questionId: UUID): Quiz {
        (questions as MutableList).removeIf { it.id == questionId }
        return this
    }

    fun startQuestion(questionId: UUID): Quiz {
        participants.forEach { it.turn = false }
        questions
                .filter { it.pending && it.id != questionId }
                .forEach {
                    it.pending = false
                    it.alreadyPlayed = true
                }
        questions.filter { it.id == questionId }[0].pending = !questions.filter { it.id == questionId }[0].pending
        return this
    }

    private fun checkParticipant(participant: Participant, participantId: UUID?): Boolean {
        return if (participantId == null) {
            participant.turn
        } else {
            participant.id == participantId
        }
    }

    fun answeredCorrect(participantId: UUID?): Quiz {
        participants
                .filter { checkParticipant(it, participantId) }
                .forEach { it.points = it.points + 2 }
        return this
    }

    fun answeredIncorrect(participantId: UUID?): Quiz {
        participants
                .filter { checkParticipant(it, participantId) }
                .forEach { it.points = (it.points - 1).coerceAtLeast(0) }
        return this
    }

    fun reopenQuestion(): Quiz {
        participants.forEach { it.turn = false }
        return this
    }

    fun finishQuiz(): Quiz {
        finished = true
        return this
    }

    fun setTimestamp(timestamp: Long): Quiz {
        this.timestamp = timestamp
        return this
    }

    fun getTimestamp(): Long {
        return timestamp
    }

}
