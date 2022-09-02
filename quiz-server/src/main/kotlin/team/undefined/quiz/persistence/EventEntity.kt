package team.undefined.quiz.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "events")
data class EventEntity(
        @Id var id: String? = null,
        var aggregateId: String,
        var type: String,
        var sequenceNumber: Long,
        var createdAt: LocalDateTime = LocalDateTime.now(),
        var domainEvent: String
)
