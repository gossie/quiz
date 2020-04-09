package team.undefined.quiz.core

data class CreateQuizCommand(val quizName: String)

data class CreateQuestionCommand(val quizId: Long, val question: String, val imageUrl: String? = null)

data class CreateParticipantCommand(val quizId: Long, val participantName: String)

data class AskQuestionCommand(val quizId: Long, val questiomId: Long)

data class BuzzerCommand(val quizId: Long, val participantId: Long)

data class AnswerCommand(val quizId: Long, val answer: Answer) {
    enum class Answer {
        CORRECT,
        INCORRECT
    }
}

data class ReopenCurrentQuestionCommand(val quizId: Long)
