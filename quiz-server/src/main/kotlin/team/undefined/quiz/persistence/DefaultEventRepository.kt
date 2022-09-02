package team.undefined.quiz.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import team.undefined.quiz.core.Event
import team.undefined.quiz.core.EventRepository
import java.util.UUID

@Component
class DefaultEventRepository(private val eventEntityRepository: EventEntityRepository,
                             private val objectMapper: ObjectMapper) : EventRepository {

    override fun storeEvent(event: Event): Mono<Event> {
        return eventEntityRepository.save(EventEntity(aggregateId = event.quizId.toString(), type = event.javaClass.name, sequenceNumber = event.sequenceNumber, domainEvent = objectMapper.writeValueAsString(event)))
                .map { objectMapper.readValue(it.domainEvent, Class.forName(it.type)) }
                .map { Event::class.java.cast(it) }
    }

    override fun determineEvents(quizId: UUID): Flux<Event> {
        //OrderBySequenceNumberAscCreatedAtAsc
        return eventEntityRepository.findAllByAggregateId(quizId.toString())
                .map { objectMapper.readValue(it.domainEvent, Class.forName(it.type)) }
                .map { Event::class.java.cast(it) }
    }

    override fun determineEvents(): Flux<Event> {
        return eventEntityRepository.findAll(Sort.by(Sort.Direction.ASC, "createdAt"))
                .map { objectMapper.readValue(it.domainEvent, Class.forName(it.type)) }
                .map { Event::class.java.cast(it) }
    }

    override fun deleteEvents(quizId: UUID): Mono<Void> {
        return eventEntityRepository.deleteAllByAggregateId(quizId.toString())
    }

    override fun determineQuizIds(): Flux<UUID> {
        // TODO: this should be handled by the database
        return eventEntityRepository.findAll()
                .map { it.aggregateId }
                .distinct()
                .map { UUID.fromString(it) }
    }

    override fun undoLastAction(quizId: UUID): Mono<Event> {
        return eventEntityRepository.findAllByAggregateId(quizId.toString(), Sort.by(Sort.Direction.DESC, "sequenceNumber", "createdAt"))
                .take(1)
                .toMono()
                .flatMap { deleteEvent(it) }
                .map { objectMapper.readValue(it.domainEvent, Class.forName(it.type)) }
                .map { Event::class.java.cast(it) }
    }

    private fun deleteEvent(eventEntity: EventEntity): Mono<EventEntity> {
        return eventEntityRepository.deleteById(eventEntity.id!!)
                .then(Mono.just(eventEntity))
    }

}
