package team.undefined.quiz.core

import java.util.*
import kotlin.collections.ArrayList

data class Quiz(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val participants: List<Participant> = ArrayList(),
    val questions: List<Question> = ArrayList(),
    val finished: Boolean = false,
    val timestamp: Long = Date().time,
    val undoPossible: Boolean = false,
    val redoPossible: Boolean = false
) {

    val pendingQuestion: Question?
        get() = questions.firstOrNull { it.pending }

    fun nobodyHasBuzzered(): Boolean {
        return participants
                .none { it.turn }
    }

    fun decreaseTimeToAnswer(questionId: UUID): Quiz {
        val newQuestions = questions.map {
            if (it.id == questionId) {
                it.decreaseSecondsLeft()
            } else {
                it
            }
        }

        return Quiz(id, name, participants, newQuestions, finished, timestamp, undoPossible)
    }

    fun select(participantId: UUID): Quiz {
        val newParticipants = participants
            .map {
                if (it.id == participantId) {
                    Participant(it.id, it.name, true, it.points, it.revealAllowed)
                } else {
                    it
                }
            }
        return Quiz(id, name, newParticipants, questions, finished, timestamp, undoPossible, redoPossible)
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
        val newParticipants = participants
            .map {
                if (it.id == participantId) {
                    Participant(it.id, it.name, it.turn, it.points, !it.revealAllowed)
                } else {
                    it
                }
            }
        return Quiz(id, name, newParticipants, questions, finished, timestamp, undoPossible, redoPossible)
    }

    fun hasNoParticipantWithName(name: String): Boolean {
        return participants.none { it.name == name }
    }

    fun hasParticipantWithId(id: UUID): Boolean {
        return participants.any { it.id == id }
    }

    fun addParticipantIfNecessary(participant: Participant): Quiz {
        val newParticipants = if (hasNoParticipantWithName(participant.name)) {
            val tmp = ArrayList(participants)
            tmp.add(participant)
            tmp
        } else {
            participants
        }
        return Quiz(id, name, newParticipants, questions, finished, timestamp, undoPossible, redoPossible)
    }

    fun deleteParticipant(participantId: UUID): Quiz {
        val newParticipants = participants.filter { it.id != participantId }
        questions
                .filter { it.estimates?.containsKey(participantId) ?: false }
                .forEach { (it.estimates as MutableMap).remove(participantId) }
        return Quiz(id, name, newParticipants, questions, finished, timestamp, undoPossible, redoPossible)
    }

    fun addQuestion(question: Question): Quiz {
        val prevId = if (questions.isNotEmpty()) {
            questions.last().id
        } else {
            null
        }

        val newQuestions = ArrayList(questions)
        newQuestions.add(question.setPreviousQuestionId(prevId))

        return Quiz(id, name, participants, newQuestions, finished, timestamp, undoPossible)
    }

    fun editQuestion(question: Question): Quiz {
        val questionsWithoutEdited = ArrayList(questions.filter { it.id != question.id })
        val indexOfPrevious = questionsWithoutEdited.indexOfFirst { it.id == question.previousQuestionId }
        questionsWithoutEdited.add(indexOfPrevious + 1, question)

        val newQuestions = questionsWithoutEdited.mapIndexed { index, it ->
            val p = if (index == 0) {
                null
            } else {
                questionsWithoutEdited[index - 1].id
            }

            it.setPreviousQuestionId(p)
        }

        return Quiz(id, name, participants, newQuestions, finished, timestamp, undoPossible, redoPossible)
    }

    fun deleteQuestion(questionId: UUID): Quiz {
        val newQuestions = questions.filter { it.id != questionId }
        return Quiz(id, name, participants, newQuestions, finished, timestamp, undoPossible, redoPossible)
    }

    fun startQuestion(questionId: UUID): Quiz {
        val newParticipants = participants.map { Participant(it.id, it.name, false, it.points, it.revealAllowed) }
        val newQuestions = questions
                .map {
                    if (it.pending && it.id != questionId) {
                        it.finish()
                    } else if (it.id == questionId) {
                        it.start()
                    } else {
                        it
                    }
                }

        return Quiz(id, name, newParticipants, newQuestions, finished, timestamp, undoPossible, redoPossible)
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
        val newParticipants = participants
            .map {
                if (checkParticipant(it, participantId)) {
                    Participant(it.id, it.name, it.turn, it.points + 2, it.revealAllowed)
                } else {
                    it
                }
            }
        return Quiz(id, name, newParticipants, questions, finished, timestamp, undoPossible, redoPossible)
    }

    fun answeredIncorrect(participantId: UUID?): Quiz {
        val newParticipants = participants
            .map {
                if (checkParticipant(it, participantId)) {
                    Participant(it.id, it.name, it.turn, (it.points - 1).coerceAtLeast(0), it.revealAllowed)
                } else {
                    it
                }
            }
        return Quiz(id, name, newParticipants, questions, finished, timestamp, undoPossible, redoPossible)
    }

    fun reopenQuestion(): Quiz {
        val newParticipants = participants.map { Participant(it.id, it.name, false, it.points, it.revealAllowed) }
        val newQuestions = questions
                .map {
                    if (it.pending) {
                        it.reopen()
                    } else {
                        it
                    }
                }
        return Quiz(id, name, newParticipants, newQuestions, finished, timestamp, undoPossible, redoPossible)
    }

    fun revealAnswersOfCurrentQuestion(): Quiz {
        val newQuestions = questions
                .map {
                    if (it.pending) {
                        it.reveal()
                    } else {
                        it
                    }
                }
        return Quiz(id, name, participants, newQuestions, finished, timestamp, undoPossible, redoPossible)
    }

    fun finishQuiz(): Quiz {
        return Quiz(id, name, participants, questions, true, timestamp, undoPossible, redoPossible)
    }

    fun setQuizStatistics(quizStatistics: QuizStatistics): Quiz {
        return Quiz(id, name, participants, questions, finished, timestamp, undoPossible, redoPossible)
    }

    fun setTimestamp(timestamp: Long): Quiz {
        return Quiz(id, name, participants, questions, finished, timestamp, undoPossible, redoPossible)
    }

    fun setUndoPossible(): Quiz {
        return Quiz(id, name, participants, questions, finished, timestamp, true, redoPossible)
    }

    fun setRedoPossible(redoPossible: Boolean): Quiz {
        return Quiz(id, name, participants, questions, finished, timestamp, undoPossible, redoPossible)
    }

}
