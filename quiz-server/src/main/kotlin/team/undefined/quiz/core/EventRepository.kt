package team.undefined.quiz.core

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface EventRepository {

    fun storeEvent(event: Event): Mono<Event>

    fun determineEvents(quizId: UUID): Flux<Event>

    fun determineEvents(): Flux<Event>

}
