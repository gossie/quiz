package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import org.springframework.stereotype.Component
import reactor.core.publisher.EmitterProcessor
import reactor.core.publisher.Flux
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
        return observables.computeIfAbsent(quizId) {
            val emitter: EmitterProcessor<Quiz> = EmitterProcessor.create(false)
            emitter.doAfterTerminate { observables.remove(quizId) }
            emitter.doOnCancel { observables.remove(quizId) }
            emitter
        }
    }

    private fun emitQuiz(quiz: Quiz) {
        observables.computeIfAbsent(quiz.id) {
            val emitter: EmitterProcessor<Quiz> = EmitterProcessor.create(false)
            emitter.doAfterTerminate { observables.remove(quiz.id) }
            emitter.doOnCancel { observables.remove(quiz.id) }
            emitter
        }.onNext(quiz)
    }

    fun removeObserver(quizId: UUID) {
        observables.remove(quizId)
    }

}