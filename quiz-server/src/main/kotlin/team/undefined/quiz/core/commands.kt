package team.undefined.quiz.core

import java.util.UUID

data class CreateQuizCommand(val quizId: UUID, val quiz: Quiz)

data class CreateQuestionCommand(val quizId: UUID, val question: Question)

data class CreateParticipantCommand(val quizId: UUID, val participant: Participant)

data class AskQuestionCommand(val quizId: UUID, val questionId: UUID)

data class BuzzerCommand(val quizId: UUID, val participantId: UUID)

data class AnswerCommand(val quizId: UUID, val answer: Answer) {
    enum class Answer(private val handler: (Quiz) -> Quiz) {
        CORRECT({it.answeredCorrect()}),
        INCORRECT({it.answeredInorrect()});

        fun performAnswer(quiz: Quiz): Quiz {
            return handler(quiz)
        }
    }
}

data class ReopenCurrentQuestionCommand(val quizId: UUID)
