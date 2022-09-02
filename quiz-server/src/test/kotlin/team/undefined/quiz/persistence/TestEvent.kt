package team.undefined.quiz.persistence

import com.fasterxml.jackson.annotation.JsonProperty
import team.undefined.quiz.core.Event
import team.undefined.quiz.core.Quiz
import java.util.*

data class TestEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("sequenceNumber") override val sequenceNumber: Long, @JsonProperty("timestamp") override val timestamp: Long, @JsonProperty("payload") val payload: Map<String, String>) :
    Event {
    override fun process(quiz: Quiz): Quiz {
        return quiz.setTimestamp(Date().time)
    }
}