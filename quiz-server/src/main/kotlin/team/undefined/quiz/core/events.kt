package team.undefined.quiz.core

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Date
import java.util.UUID

interface Event {
    val quizId: UUID
    val timestamp: Long

    fun process(quiz: Quiz): Quiz
}

data class QuizCreatedEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("quiz") val quiz: Quiz, @JsonProperty("timestamp") override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return this.quiz.setTimestamp(timestamp)
    }
}

data class QuestionCreatedEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("question") val question: Question, @JsonProperty("timestamp") override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.addQuestion(question)
                .setTimestamp(timestamp)
    }
}

data class QuestionEditedEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("question") val question: Question, @JsonProperty("timestamp") override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.editQuestion(question)
                .setTimestamp(timestamp)
    }
}

data class QuestionDeletedEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("questionId") val questionId: UUID, @JsonProperty("timestamp") override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.deleteQuestion(questionId)
                .setTimestamp(timestamp)
    }
}

data class ParticipantCreatedEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("participant") val participant: Participant, @JsonProperty("timestamp") override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.addParticipantIfNecessary(participant)
                .setTimestamp(timestamp)
    }
}

data class QuestionAskedEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("questionId") val questionId: UUID, @JsonProperty("timestamp") override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.startQuestion(questionId)
                .setTimestamp(timestamp)
    }
}

data class TimeToAnswerDecreasedEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("questionId") val questionId: UUID, @JsonProperty("timestamp") override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.decreaseTimeToAnswer(questionId)
                .setTimestamp(timestamp)
    }
}

data class BuzzeredEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("participantId") val participantId: UUID, @JsonProperty("timestamp") override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.select(participantId)
                .setTimestamp(timestamp)
    }
}

data class EstimatedEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("participantId") val participantId: UUID, @JsonProperty("estimatedValue") val estimatedValue: String, @JsonProperty("timestamp") override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.estimate(participantId, estimatedValue)
                .setTimestamp(timestamp)
    }
}

data class ToggleAnswerRevealAllowedEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("participantId") val participantId: UUID, @JsonProperty("timestamp") override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.toggleAnswerRevealAllowed(participantId)
                .setTimestamp(timestamp)
    }
}

data class AnsweredEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("participantId") val participantId: UUID?, @JsonProperty("answer") val answer: AnswerCommand.Answer, @JsonProperty("timestamp") override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return answer.performAnswer(quiz, participantId)
                .setTimestamp(timestamp)
    }
}

data class CurrentQuestionReopenedEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("timestamp") override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.reopenQuestion()
                .setTimestamp(timestamp)
    }
}

data class AnswersRevealedEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("timestamp") override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.revealAnswersOfCurrentQuestion()
                .setTimestamp(timestamp)
    }
}

data class QuizFinishedEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("timestamp") override val timestamp: Long = Date().time): Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.finishQuiz()
                .setTimestamp(timestamp)
    }
}

data class QuizDeletedEvent(override val quizId: UUID, override val timestamp: Long = Date().time): Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.setTimestamp(timestamp)
    }
}
