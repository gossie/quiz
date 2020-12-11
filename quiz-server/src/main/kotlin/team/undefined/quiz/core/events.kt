package team.undefined.quiz.core

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Date
import java.util.UUID

interface Event {
    val quizId: UUID
    val sequenceNumber: Int
    val timestamp: Long

    fun process(quiz: Quiz): Quiz
}

data class QuizCreatedEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("quiz") val quiz: Quiz, @JsonProperty("sequenceNumber") override val sequenceNumber: Int = 0, @JsonProperty("timestamp") override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return this.quiz.setTimestamp(timestamp)
            .setSequenceNumber(sequenceNumber)
    }
}

data class QuestionCreatedEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("question") val question: Question, @JsonProperty("sequenceNumber") override val sequenceNumber: Int, @JsonProperty("timestamp") override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.addQuestion(question)
            .setUndoPossible()
            .setTimestamp(timestamp)
            .setSequenceNumber(sequenceNumber)
    }
}

data class QuestionEditedEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("question") val question: Question, @JsonProperty("sequenceNumber") override val sequenceNumber: Int, @JsonProperty("timestamp") override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.editQuestion(question)
            .setUndoPossible()
            .setTimestamp(timestamp)
            .setSequenceNumber(sequenceNumber)
    }
}

data class QuestionDeletedEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("questionId") val questionId: UUID, @JsonProperty("sequenceNumber") override val sequenceNumber: Int, @JsonProperty("timestamp") override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.deleteQuestion(questionId)
            .setUndoPossible()
            .setTimestamp(timestamp)
            .setSequenceNumber(sequenceNumber)
    }
}

data class ParticipantCreatedEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("participant") val participant: Participant, @JsonProperty("sequenceNumber") override val sequenceNumber: Int, @JsonProperty("timestamp") override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.addParticipantIfNecessary(participant)
            .setUndoPossible()
            .setTimestamp(timestamp)
            .setSequenceNumber(sequenceNumber)
    }
}

data class ParticipantDeletedEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("participantId") val participantId: UUID, @JsonProperty("sequenceNumber") override val sequenceNumber: Int, @JsonProperty("timestamp") override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.deleteParticipant(participantId)
            .setUndoPossible()
            .setTimestamp(timestamp)
            .setSequenceNumber(sequenceNumber)
    }
}

data class QuestionAskedEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("questionId") val questionId: UUID, @JsonProperty("sequenceNumber") override val sequenceNumber: Int, @JsonProperty("timestamp") override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.startQuestion(questionId)
            .setUndoPossible()
            .setTimestamp(timestamp)
            .setSequenceNumber(sequenceNumber)
    }
}

data class TimeToAnswerDecreasedEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("questionId") val questionId: UUID, @JsonProperty("sequenceNumber") override val sequenceNumber: Int, @JsonProperty("timestamp") override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.decreaseTimeToAnswer(questionId)
            .setTimestamp(timestamp)
            .setSequenceNumber(sequenceNumber)
    }
}

data class BuzzeredEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("participantId") val participantId: UUID, @JsonProperty("sequenceNumber") override val sequenceNumber: Int, @JsonProperty("timestamp") override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.select(participantId)
            .setUndoPossible()
            .setTimestamp(timestamp)
            .setSequenceNumber(sequenceNumber)
    }
}

data class EstimatedEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("participantId") val participantId: UUID, @JsonProperty("estimatedValue") val estimatedValue: String, @JsonProperty("sequenceNumber") override val sequenceNumber: Int, @JsonProperty("timestamp") override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.estimate(participantId, estimatedValue)
            .setUndoPossible()
            .setTimestamp(timestamp)
            .setSequenceNumber(sequenceNumber)
    }
}

data class ChoiceSelectedEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("participantId") val participantId: UUID, @JsonProperty("choiceId") val choiceId: UUID, @JsonProperty("sequenceNumber") override val sequenceNumber: Int, @JsonProperty("timestamp") override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.selectChoice(participantId, choiceId)
            .setTimestamp(timestamp)
            .setSequenceNumber(sequenceNumber)
    }
}

data class ToggleAnswerRevealAllowedEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("participantId") val participantId: UUID, @JsonProperty("sequenceNumber") override val sequenceNumber: Int, @JsonProperty("timestamp") override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.toggleAnswerRevealAllowed(participantId)
            .setUndoPossible()
            .setTimestamp(timestamp)
            .setSequenceNumber(sequenceNumber)
    }
}

data class AnsweredEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("participantId") val participantId: UUID, @JsonProperty("answer") val answer: AnswerCommand.Answer, @JsonProperty("sequenceNumber") override val sequenceNumber: Int, @JsonProperty("timestamp") override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return answer.performAnswer(quiz, participantId)
            .setUndoPossible()
            .setTimestamp(timestamp)
            .setSequenceNumber(sequenceNumber)
    }
}

data class CurrentQuestionReopenedEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("sequenceNumber") override val sequenceNumber: Int, @JsonProperty("timestamp") override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.reopenQuestion()
            .setUndoPossible()
            .setTimestamp(timestamp)
            .setSequenceNumber(sequenceNumber)
    }
}

data class AnswersRevealedEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("sequenceNumber") override val sequenceNumber: Int, @JsonProperty("timestamp") override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.revealAnswersOfCurrentQuestion()
            .setUndoPossible()
            .setTimestamp(timestamp)
            .setSequenceNumber(sequenceNumber)
    }
}

data class QuizFinishedEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("sequenceNumber") override val sequenceNumber: Int, @JsonProperty("timestamp") override val timestamp: Long = Date().time): Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.finishQuiz()
            .setUndoPossible()
            .setTimestamp(timestamp)
            .setSequenceNumber(sequenceNumber)
    }
}

data class QuizDeletedEvent(override val quizId: UUID, override val sequenceNumber: Int, override val timestamp: Long = Date().time): Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.setTimestamp(timestamp)
            .setSequenceNumber(sequenceNumber)
    }
}
