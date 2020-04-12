package team.undefined.quiz.persistence

import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import team.undefined.quiz.core.Event
import team.undefined.quiz.core.EventRepository
import java.util.UUID

@Component
class DefaultEventRepository(private val eventEntityRepository: EventEntityRepository) : EventRepository {

    override fun storeEvent(event: Event): Mono<Event> {
        TODO("Not yet implemented")
    }

    override fun determineEvents(quizId: UUID): Flux<Event> {
        TODO("Not yet implemented")
    }

}
