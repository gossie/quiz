package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.EmitterProcessor
import reactor.core.publisher.Flux
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Semaphore

@Component
class QuizProjection(eventBus: EventBus,
                     private val quizStatisticsProvider: QuizStatisticsProvider,
                     private val eventRepository: EventRepository) {

    private val logger = LoggerFactory.getLogger(QuizProjection::class.java)

    private val quizCache = ConcurrentHashMap<UUID, Quiz>()
    private val observables = ConcurrentHashMap<UUID, EmitterProcessor<Quiz>>()
    private val locks = ConcurrentHashMap<UUID, Semaphore>()

    init {
        eventBus.register(this)
    }

    @Subscribe
    fun handleQuizCreation(event: QuizCreatedEvent) {
        quizCache[event.quizId] = event.process(event.quiz)
        emitQuiz(quizCache[event.quizId]!!)
        locks.computeIfAbsent(event.quizId) { Semaphore(1) }
    }

    @Subscribe
    fun handleQuestionCreation(event: QuestionCreatedEvent) = handleEvent(event)

    @Subscribe
    fun handleQuestionEdit(event: QuestionEditedEvent) = handleEvent(event)

    @Subscribe
    fun handleQuestionDeletion(event: QuestionDeletedEvent) = handleEvent(event)

    @Subscribe
    fun handleParticipantCreation(event: ParticipantCreatedEvent) = handleEvent(event)

    @Subscribe
    fun handleQuestionAsked(event: QuestionAskedEvent) = handleEvent(event)

    @Subscribe
    fun handleTimeToAnswerDecrease(event: TimeToAnswerDecreasedEvent) = handleEvent(event)

    @Subscribe
    fun handleBuzzer(event: BuzzeredEvent) = handleEvent(event)

    @Subscribe
    fun handleEstimation(event: EstimatedEvent) = handleEvent(event)

    @Subscribe
    fun handleAnswerRevealToggle(event: ToggleAnswerRevealAllowedEvent) = handleEvent(event)

    @Subscribe
    fun handleAnswer(event: AnsweredEvent) = handleEvent(event)

    @Subscribe
    fun handleRevealOfAnswers(event: AnswersRevealedEvent) = handleEvent(event)

    @Subscribe
    fun handleReopenedQuestion(event: CurrentQuestionReopenedEvent) = handleEvent(event)

    @Subscribe
    fun handleFinishedQuiz(event: QuizFinishedEvent) {
        val quiz = quizCache[event.quizId]
        if (quiz == null) {
            eventRepository.determineEvents(event.quizId)
                    .reduce(Quiz(name = "")) { q: Quiz, e: Event -> e.process(q)}
                    .subscribe {
                        if (it.getTimestamp() < event.timestamp) {
                            quizCache[event.quizId] = event.process(it)
                        } else {
                            quizCache[event.quizId] = it
                        }
                        quizStatisticsProvider.generateStatistics(event.quizId)
                                .subscribe {
                                    quizCache[event.quizId]!!.quizStatistics = it
                                    emitQuiz(quizCache[event.quizId]!!)
                                }
                    }
        } else if (quiz.getTimestamp() < event.timestamp) {
            quizCache[event.quizId] = event.process(quiz)
            quizStatisticsProvider.generateStatistics(event.quizId)
                    .subscribe {
                        quizCache[event.quizId]!!.quizStatistics = it
                        emitQuiz(quizCache[event.quizId]!!)
                    }
        }
    }

    @Subscribe
    fun handleQuizDeletion(event: QuizDeletedEvent) {
        quizCache.remove(event.quizId)
        locks.remove(event.quizId)
        observables.remove(event.quizId)
    }

    @Subscribe
    fun handleForceEmitCommand(command: ForceEmitCommand) {
        eventRepository.determineEvents(command.quizId)
                .reduce(Quiz(name = "")) { q: Quiz, e: Event -> e.process(q) }
                .subscribe { emitQuiz(it) }
    }

    private fun handleEvent(event: Event) {
        logger.info("handling event {}", event)
        try {
            locks.computeIfAbsent(event.quizId) { Semaphore(1) }.acquire()
            val quiz = quizCache[event.quizId]
            if (quiz == null) {
                eventRepository.determineEvents(event.quizId)
                        .reduce(Quiz(name = "")) { q: Quiz, e: Event -> e.process(q) }
                        .subscribe {
                            if (it.getTimestamp()!! < event.timestamp) {
                                quizCache[event.quizId] = event.process(it)
                            } else {
                                quizCache[event.quizId] = it
                            }
                            emitQuiz(quizCache[event.quizId]!!)
                        }
            } else {
                quizCache[event.quizId] = event.process(quiz)
                emitQuiz(quizCache[event.quizId]!!)
            }
        } finally {
            locks[event.quizId]!!.release()
        }
        logger.info("handled event {}", event)
    }

    fun observeQuiz(quizId: UUID): Flux<Quiz> {
        return observables.computeIfAbsent(quizId) { EmitterProcessor.create(false) }
    }

    private fun emitQuiz(quiz: Quiz) {
        observables.computeIfAbsent(quiz.id) { EmitterProcessor.create(false) }
                .onNext(quiz)
    }

    fun removeObserver(quizId: UUID) {
        observables.remove(quizId)
    }

    fun determineQuiz(quizId: UUID): Quiz? {
        return quizCache[quizId]
    }

}