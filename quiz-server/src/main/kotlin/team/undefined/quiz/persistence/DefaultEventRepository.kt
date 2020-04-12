package team.undefined.quiz.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import team.undefined.quiz.core.Event
import team.undefined.quiz.core.EventRepository
import team.undefined.quiz.core.QuizCreatedEvent
import java.time.LocalDateTime
import java.util.UUID

@Component
class DefaultEventRepository(private val eventEntityRepository: EventEntityRepository,
                             private val objectMapper: ObjectMapper) : EventRepository {

    override fun storeEvent(event: Event): Mono<Event> {
        return eventEntityRepository.save(EventEntity(aggregateId = event.quizId, type = event.javaClass.name, domainEvent = objectMapper.writeValueAsString(event)))
                .map { objectMapper.readValue(it.domainEvent, Class.forName(it.type)) }
                .map { Event::class.java.cast(it) }
    }

    override fun determineEvents(quizId: UUID): Flux<Event> {
        return eventEntityRepository.findAllByAggregateId(quizId.toString())
                .map { objectMapper.readValue(it.domainEvent, Class.forName(it.type)) }
                .map { Event::class.java.cast(it) }
    }

}
