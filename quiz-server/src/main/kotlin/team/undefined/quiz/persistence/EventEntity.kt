package team.undefined.quiz.persistence

data class EventEntity(var id: Long, var aggregateId: Long, var createdAt: Long, var domainEvent: String)
