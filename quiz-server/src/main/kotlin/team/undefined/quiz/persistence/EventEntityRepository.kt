package team.undefined.quiz.persistence

import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface EventEntityRepository : ReactiveMongoRepository<EventEntity, String> {

    fun findAllByAggregateId(quizId: String, sort: Sort = Sort.by(Sort.Direction.ASC, "sequenceNumber", "createdAt")): Flux<EventEntity>

    fun deleteAllByAggregateId(quizId: String): Mono<Void>

    //fun findAllAggregateIdsDistinct(): Flux<String>

    //fun findTop1ByAggregateIdOrderBySequenceNumberDescCreatedAtDesc(quizId: String): Mono<EventEntity>

}
