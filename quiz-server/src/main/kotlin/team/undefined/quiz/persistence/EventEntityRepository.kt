package team.undefined.quiz.persistence

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Repository
interface EventEntityRepository : ReactiveCrudRepository<EventEntity, Long> {

    @Query("SELECT * FROM event_entity ORDER BY created_at ASC")
    fun findAllOrdered(): Flux<EventEntity>

    @Query("SELECT * FROM event_entity WHERE aggregate_id = :quizId ORDER BY created_at ASC")
    fun findAllByAggregateId(quizId: String): Flux<EventEntity>

    @Query("DELETE FROM event_entity WHERE aggregate_id = :quizId")
    fun deleteAllByAggregateId(quizId: String): Mono<Void>

    @Query("SELECT DISTINCT aggregate_id FROM event_entity")
    fun findAllAggregateIds(): Flux<String>

    @Query("SELECT * FROM event_entity WHERE aggregate_id = :quizId ORDER BY created_at DESC LIMIT 1")
    fun findLastByAggregateId(quizId: String): Mono<EventEntity>

}
