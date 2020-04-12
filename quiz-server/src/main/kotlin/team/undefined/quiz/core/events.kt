package team.undefined.quiz.core

import java.util.Date
import java.util.UUID

interface Event {
    val quizId: UUID
    val timestamp: Long

    fun process(quiz: Quiz): Quiz
}

data class QuizCreatedEvent(override val quizId: UUID, val quiz: Quiz, override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz
    }
}

data class QuestionCreatedEvent(override val quizId: UUID, val question: Question, override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.addQuestion(question)
    }
}

data class ParticipantCreatedEvent(override val quizId: UUID, val participant: Participant, override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.addParticipantIfNecessary(participant)
    }
}

data class QuestionAskedEvent(override val quizId: UUID, val questionId: UUID, override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.startQuestion(questionId)
    }
}

data class BuzzeredEvent(override val quizId: UUID, val participantId: UUID, override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.select(participantId)
    }
}

data class AnsweredEvent(override val quizId: UUID, val answer: AnswerCommand.Answer, override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return answer.performAnswer(quiz)
    }
}

data class CurrentQuestionReopenedEvent(override val quizId: UUID, override val timestamp: Long = Date().time) : Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.reopenQuestion()
    }
}
