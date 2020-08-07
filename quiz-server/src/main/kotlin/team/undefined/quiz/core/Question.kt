package team.undefined.quiz.core

import java.util.UUID

data class Question(
        val id: UUID = UUID.randomUUID(),
        val question: String,
        var pending: Boolean = false,
        val imageUrl: String = "",
        val estimates: Map<UUID, String>? = null,
        var visibility: QuestionVisibility = QuestionVisibility.PRIVATE,
        var alreadyPlayed: Boolean = false,
        val category: QuestionCategory = QuestionCategory("other"),
        val initialTimeToAnswer: Int? = null,
        val secondsLeft: Int? = null
) {

    enum class QuestionVisibility(private val b: Boolean) {
        PUBLIC(true),
        PRIVATE(false);

        fun asBoolean(): Boolean {
            return b;
        }
    }

    fun estimate(participantId: UUID, estimatedValue: String) {
        (estimates as MutableMap)[participantId] = estimatedValue
    }

}
