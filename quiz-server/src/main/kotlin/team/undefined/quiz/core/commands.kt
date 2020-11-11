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

data class ToggleAnswerRevealAllowedCommand(override val quizId: UUID, val participantId: UUID) : Command

data class AnswerCommand(override val quizId: UUID, val participantId: UUID, val answer: Answer) : Command {
    enum class Answer(private val handler: (Quiz, UUID?) -> Quiz) {
        CORRECT({quiz, participantId -> quiz.answeredCorrect(participantId)}),
        INCORRECT({quiz, participantId -> quiz.answeredIncorrect(participantId)});

        fun performAnswer(quiz: Quiz, participantId: UUID?): Quiz {
            return handler(quiz, participantId)
        }
    }
}

data class ReopenCurrentQuestionCommand(override val quizId: UUID) : Command

data class RevealAnswersCommand(override val quizId: UUID) : Command

data class FinishQuizCommand(override val quizId: UUID) : Command

data class DeleteQuizCommand(override val quizId: UUID) : Command
