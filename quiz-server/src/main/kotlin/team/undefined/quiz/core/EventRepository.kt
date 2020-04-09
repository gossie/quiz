package team.undefined.quiz.core

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface EventRepository {

    fun storeEvent(event: Event): Mono<Event>

    fun determineEvents(quizId: Long): Flux<Event>

}
