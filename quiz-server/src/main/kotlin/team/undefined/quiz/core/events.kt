package team.undefined.quiz.core

interface Event {
    val quizId: Long
    val timestamp: Long
}

data class QuizCreatedEvent(override val quizId: Long, override val timestamp: Long, val quiz: Quiz) : Event

data class QuestionCreatedEvent(override val quizId: Long, override val timestamp: Long, val question: Question) : Event

data class ParticipantCreatedEvent(override val quizId: Long, override val timestamp: Long, val participant: Participant) : Event

data class QuestionAskedEvent(override val quizId: Long, override val timestamp: Long, val question: Question) : Event

data class BuzzeredEvent(override val quizId: Long, override val timestamp: Long, val participant: Participant) : Event

data class AnsweredEvent(override val quizId: Long, override val timestamp: Long, val answer: AnswerCommand.Answer) : Event

data class QuestionReopenedEvent(override val quizId: Long, override val timestamp: Long, val question: Question) : Event
