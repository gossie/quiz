package team.undefined.quiz.core

import java.util.UUID

data class Question(
        val id: UUID = UUID.randomUUID(),
        val question: String,
        var pending: Boolean = false,
        val imageUrl: String = "",
        var visibility: QuestionVisibility = QuestionVisibility.PRIVATE,
        var alreadyPlayed: Boolean = false,
        val category: QuestionCategory = QuestionCategory("other"),
        val initialTimeToAnswer: Int? = null,
        var secondsLeft: Int? = initialTimeToAnswer,
        var revealed: Boolean = false,
        var previousQuestionId: UUID? = null,
        var choices: List<Choice>? = null,
        val estimates: Map<UUID, String>? = if(choices != null) { HashMap() } else { null },
        val correctAnswer: String? = null
) {

    enum class QuestionVisibility(private val b: Boolean) {
        PUBLIC(true),
        PRIVATE(false);

        fun asBoolean(): Boolean {
            return b;
        }
    }

    fun estimate(participantId: UUID, estimatedValue: String) {
        if (estimates != null) {
            (estimates as MutableMap)[participantId] = estimatedValue
        }
    }

}
