package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import org.springframework.stereotype.Component
import reactor.core.publisher.EmitterProcessor
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
class QuizProjection(eventBus: EventBus,
                     private val eventRepository: EventRepository) {

    private val quizCache = ConcurrentHashMap<UUID, Quiz>()
    private val observables = ConcurrentHashMap<UUID, EmitterProcessor<Quiz>>()

    init {
        eventBus.register(this)
    }

    @Subscribe
    fun handleQuizCreation(event: QuizCreatedEvent) {
        quizCache[event.quizId] = event.process(event.quiz)
        emitQuiz(quizCache[event.quizId]!!)
    }

    @Subscribe
    fun handleQuestionCreation(event: QuestionCreatedEvent) = handleEvent(event)

    @Subscribe
    fun handleParticipantCreation(event: ParticipantCreatedEvent) = handleEvent(event)

    @Subscribe
    fun handleQuestionAsked(event: QuestionAskedEvent) = handleEvent(event)

    @Subscribe
    fun handleBuzzer(event: BuzzeredEvent) = handleEvent(event)

    @Subscribe
    fun handleAnswer(event: AnsweredEvent) = handleEvent(event)

    @Subscribe
    fun handleReopenedQuestion(event: CurrentQuestionReopenedEvent) = handleEvent(event)

    private fun handleEvent(event: Event) {
        val quiz = quizCache[event.quizId]
        if (quiz == null) {
            eventRepository.determineEvents(event.quizId)
                    .reduce(Quiz(name = "")) { q: Quiz, e: Event -> e.process(q)}
                    .subscribe {
                        if (it.getTimestamp()!! < event.timestamp) {
                            quizCache[event.quizId] = event.process(it)
                        } else {
                            quizCache[event.quizId] = it
                        }
                        emitQuiz(quizCache[event.quizId]!!)
                    }
        } else if (quiz.getTimestamp()!! < event.timestamp) {
            quizCache[event.quizId] = event.process(quiz)
            emitQuiz(quizCache[event.quizId]!!)
        }
    }

    fun observeQuiz(quizId: UUID): Flux<Quiz> {
//        Mono.just(quizCache[quizId])
//                .m
        val observable = observables.computeIfAbsent(quizId) { EmitterProcessor.create(false) }
//        return observable
//                .last()
        return observable
    }

    private fun emitQuiz(quiz: Quiz) {
        observables.computeIfAbsent(quiz.id) { EmitterProcessor.create(false) }
                .onNext(quiz)
    }

    fun removeObserver(quizId: UUID) {
        observables.remove(quizId)
    }

}