package team.undefined.quiz.core

import java.util.Date
import java.util.UUID

interface Event {
    val quizId: UUID
    val timestamp: Long
}

data class QuizCreatedEvent(override val quizId: UUID, val quiz: Quiz, override val timestamp: Long = Date().time) : Event

data class QuestionCreatedEvent(override val quizId: UUID, val question: Question, override val timestamp: Long = Date().time) : Event

data class ParticipantCreatedEvent(override val quizId: UUID, val participant: Participant, override val timestamp: Long = Date().time) : Event

data class QuestionAskedEvent(override val quizId: UUID, val questionId: UUID, override val timestamp: Long = Date().time) : Event

data class BuzzeredEvent(override val quizId: UUID, val participantId: UUID, override val timestamp: Long = Date().time) : Event

data class AnsweredEvent(override val quizId: UUID, val answer: AnswerCommand.Answer, override val timestamp: Long = Date().time) : Event

data class CurrentQuestionReopenedEvent(override val quizId: UUID, override val timestamp: Long = Date().time) : Event
