package team.undefined.quiz.persistence

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface EventEntityRepository : ReactiveCrudRepository<EventEntity, Long>
