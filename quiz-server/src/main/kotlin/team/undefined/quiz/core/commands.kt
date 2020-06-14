package team.undefined.quiz.core

import java.util.UUID

interface Command {
    val quizId: UUID
}

data class CreateQuizCommand(override val quizId: UUID, val quiz: Quiz) : Command

data class CreateQuestionCommand(override val quizId: UUID, val question: Question) : Command

data class EditQuestionCommand(override val quizId: UUID, val questionId: UUID, val question: Question) : Command

data class DeleteQuestionCommand(override val quizId: UUID, val questionId: UUID) : Command

data class CreateParticipantCommand(override val quizId: UUID, val participant: Participant) : Command

data class ForceEmitCommand(override val quizId: UUID) : Command

data class AskQuestionCommand(override val quizId: UUID, val questionId: UUID) : Command

data class EstimationCommand(override val quizId: UUID, val participantId: UUID, val estimatedValue: String) : Command

data class BuzzerCommand(override val quizId: UUID, val participantId: UUID) : Command

data class AnswerCommand(override val quizId: UUID, val answer: Answer) : Command {
    enum class Answer(private val handler: (Quiz) -> Quiz) {
        CORRECT({it.answeredCorrect()}),
        INCORRECT({it.answeredIncorrect()});

        fun performAnswer(quiz: Quiz): Quiz {
            return handler(quiz)
        }
    }
}

data class ReopenCurrentQuestionCommand(override val quizId: UUID) : Command

data class FinishQuizCommand(override val quizId: UUID) : Command

data class DeleteQuizCommand(override val quizId: UUID) : Command
