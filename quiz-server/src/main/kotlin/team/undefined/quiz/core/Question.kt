package team.undefined.quiz.core

import java.util.UUID

data class Question(
        val id: UUID = UUID.randomUUID(),
        val question: String,
        val pending: Boolean = false,
        val imageUrl: String = "",
        val visibility: QuestionVisibility = QuestionVisibility.PRIVATE,
        val alreadyPlayed: Boolean = false,
        val category: QuestionCategory = QuestionCategory("other"),
        val initialTimeToAnswer: Int? = null,
        val secondsLeft: Int? = initialTimeToAnswer,
        val revealed: Boolean = false,
        val previousQuestionId: UUID? = null,
        val choices: List<Choice>? = null,
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

    fun estimate(participantId: UUID, estimatedValue: String): Question {
        val newEstimates = if (estimates != null) {
            (estimates as MutableMap)[participantId] = estimatedValue
            val tmp = HashMap(estimates)
            tmp[participantId] = estimatedValue
            tmp
        } else {
            null
        }

        return Question(id,
            question,
            pending,
            imageUrl,
            visibility,
            alreadyPlayed,
            category,
            initialTimeToAnswer,
            secondsLeft,
            revealed,
            previousQuestionId,
            choices,
            newEstimates,
            correctAnswer
        )
    }

    fun decreaseSecondsLeft(): Question {
        return Question(
            id,
            question,
            pending,
            imageUrl,
            visibility,
            alreadyPlayed,
            category,
            initialTimeToAnswer,
            secondsLeft?.minus(1),
            revealed,
            previousQuestionId,
            choices,
            estimates,
            correctAnswer
        )
    }

    fun setPreviousQuestionId(prevId: UUID?): Question {
        return Question(
            id,
            question,
            pending,
            imageUrl,
            visibility,
            alreadyPlayed,
            category,
            initialTimeToAnswer,
            secondsLeft,
            revealed,
            prevId,
            choices,
            estimates,
            correctAnswer
        )
    }

    fun start(): Question {
        return Question(
            id,
            question,
            true,
            imageUrl,
            visibility,
            alreadyPlayed,
            category,
            initialTimeToAnswer,
            initialTimeToAnswer,
            revealed,
            previousQuestionId,
            choices,
            estimates,
            correctAnswer
        )
    }

    fun finish(): Question {
        return Question(
            id,
            question,
            false,
            imageUrl,
            visibility,
            true,
            category,
            initialTimeToAnswer,
            secondsLeft,
            revealed,
            previousQuestionId,
            choices,
            estimates,
            correctAnswer
        )
    }

    fun reopen(): Question {
        return Question(
            id,
            question,
            pending,
            imageUrl,
            visibility,
            alreadyPlayed,
            category,
            initialTimeToAnswer,
            initialTimeToAnswer,
            revealed,
            previousQuestionId,
            choices,
            estimates,
            correctAnswer
        )
    }

    fun reveal(): Question {
        return Question(
            id,
            question,
            pending,
            imageUrl,
            visibility,
            alreadyPlayed,
            category,
            initialTimeToAnswer,
            0,
            true,
            previousQuestionId,
            choices,
            estimates,
            correctAnswer
        )
    }

    fun toBeNamed(): Question {
        return Question(
            id,
            question,
            !alreadyPlayed,
            imageUrl,
            visibility,
            !alreadyPlayed,
            category,
            initialTimeToAnswer,
            secondsLeft,
            revealed,
            previousQuestionId,
            choices,
            estimates,
            correctAnswer
        )
    }

}
