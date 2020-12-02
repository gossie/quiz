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
    private var undoPossible: Boolean = false
    private var redoPossible: Boolean = false

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

    fun selectChoice(participantId: UUID, choiceId: UUID): Quiz {
        val choice = questions.find { it.pending }?.choices?.find { it.id == choiceId }
        return estimate(participantId, choice?.choice ?: "")
    }

    fun toggleAnswerRevealAllowed(participantId: UUID): Quiz {
        val participant = participants.find { it.id == participantId }
        participant?.revealAllowed = participant?.revealAllowed?.not() ?: false
        return this
    }

    fun hasNoParticipantWithName(name: String): Boolean {
        return participants.none { it.name == name }
    }

    fun hasParticipantWithId(id: UUID): Boolean {
        return participants.any { it.id == id }
    }

    fun addParticipantIfNecessary(participant: Participant): Quiz {
        if (hasNoParticipantWithName(participant.name)) {
            (participants as MutableList).add(participant)
        }
        return this
    }

    fun deleteParticipant(participantId: UUID): Quiz {
        (participants as MutableList).removeIf { it.id == participantId }
        questions
                .filter { it.estimates?.containsKey(participantId) ?: false }
                .forEach { (it.estimates as MutableMap).remove(participantId) }
        return this
    }

    fun addQuestion(question: Question): Quiz {
        question.previousQuestionId = if (questions.isNotEmpty()) {
            questions.last().id
        } else {
            null
        }

        (questions as MutableList).add(question)
        return this
    }

    fun editQuestion(question: Question): Quiz {
        (questions as MutableList).removeIf { it.id == question.id }
        val indexOfPrevious = questions.indexOfFirst { it.id == question.previousQuestionId }
        questions.add(indexOfPrevious + 1, question)

        questions.forEachIndexed { index, question ->
            val p = if (index == 0) {
                null
            } else {
                questions[index - 1].id
            }
            question.previousQuestionId = p
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

        val question = questions.filter { it.id == questionId }[0]
        question.pending = !question.pending
        question.secondsLeft = question.initialTimeToAnswer

        return this
    }

    fun currentQuestionIsBuzzerQuestion(): Boolean {
        val currentQuestion = questions.find { it.pending }
        return currentQuestion?.estimates == null
    }

    fun currentQuestionIsFreetextQuestion(): Boolean {
        val currentQuestion = questions.find { it.pending }
        return currentQuestion?.choices == null
                && currentQuestion?.estimates != null
    }

    fun currentQuestionIsMultipleChoiceQuestion(): Boolean {
        val currentQuestion = questions.find { it.pending }
        return currentQuestion?.choices?.isNotEmpty() ?: false
                && currentQuestion?.estimates != null
    }

    fun currentAnswerIsDifferent(participantId: UUID, value: String): Boolean {
        return questions.find { it.pending }
                ?.estimates
                ?.get(participantId) != value
    }

    fun currentChoiceIsDifferent(participantId: UUID, choiceId: UUID): Boolean {
        val currentQuestion = questions.find { it.pending }
        val choice = currentQuestion?.choices?.find { it.id == choiceId }
        return currentAnswerIsDifferent(participantId, choice?.choice ?: "")
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
        questions
                .filter { it.pending }
                .forEach { it.secondsLeft = it.initialTimeToAnswer }
        return this
    }

    fun revealAnswersOfCurrentQuestion(): Quiz {
        questions
                .filter { it.pending }
                .forEach {
                    it.revealed = true
                    it.secondsLeft = 0
                }
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

    fun isUndoPossible(): Boolean {
        return undoPossible
    }

    fun setUndoPossible(): Quiz {
        this.undoPossible = true
        return this
    }

    fun isRedoPossible(): Boolean {
        return redoPossible
    }

    fun setRedoPossible(redoPossible: Boolean): Quiz {
        this.redoPossible = redoPossible
        return this
    }

}
