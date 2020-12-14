package team.undefined.quiz.core

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface EventRepository {

    fun storeEvent(event: Event): Mono<Event>

    fun determineEvents(quizId: UUID): Flux<Event>

    fun determineEvents(): Flux<Event>

    fun deleteEvents(quizId: UUID): Mono<Void>

    fun determineQuizIds(): Flux<UUID>

    fun undoLastAction(quizId: UUID): Mono<Event>

}
