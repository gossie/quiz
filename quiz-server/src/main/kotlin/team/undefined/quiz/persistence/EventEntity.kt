package team.undefined.quiz.persistence

import org.springframework.data.annotation.Id
import java.time.LocalDateTime

data class EventEntity(
        @Id var id: Long? = null,
        var aggregateId: String,
        var type: String,
        var createdAt: LocalDateTime = LocalDateTime.now(),
        var domainEvent: String
)
