package team.undefined.quiz.core

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Semaphore

@Component
class DefaultQuizProjection(eventBus: EventBus,
                     private val quizStatisticsProvider: QuizStatisticsProvider,
                     private val eventRepository: EventRepository,
                     private val undoneEventsCache: UndoneEventsCache) : QuizProjection {

    private val logger = LoggerFactory.getLogger(QuizProjection::class.java)

    private val quizCache = CacheBuilder.newBuilder()
        .maximumSize(250)
        .expireAfterAccess(Duration.ofHours(1))
        .build(
            object : CacheLoader<UUID, Quiz>() {
                override fun load(key: UUID): Quiz {
                    return eventRepository.determineEvents(key)
                        .reduce(Quiz(name = "")) { q: Quiz, e: Event -> e.process(q) }
                        .block()!!
                }
            }
        )

    private val observables = ConcurrentHashMap<UUID, Sinks.Many<Quiz>>()
    private val locks = ConcurrentHashMap<UUID, Semaphore>()

    init {
        eventBus.register(this)
    }

    @Subscribe
    fun handleQuizCreation(event: QuizCreatedEvent) {
        quizCache.put(event.quizId, event.process(event.quiz))
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
    fun handleParticipantDeletion(event: ParticipantDeletedEvent) = handleEvent(event)

    @Subscribe
    fun handleQuestionAsked(event: QuestionAskedEvent) = handleEvent(event)

    @Subscribe
    fun handleTimeToAnswerDecrease(event: TimeToAnswerDecreasedEvent) = handleEvent(event)

    @Subscribe
    fun handleBuzzer(event: BuzzeredEvent) = handleEvent(event)

    @Subscribe
    fun handleEstimation(event: EstimatedEvent) = handleEvent(event)

    @Subscribe
    fun handleChoiceSelection(event: ChoiceSelectedEvent) = handleEvent(event)

    @Subscribe
    fun handleAnswerRevealToggle(event: ToggleAnswerRevealAllowedEvent) = handleEvent(event)

    @Subscribe
    fun handleAnswer(event: AnsweredEvent) = handleEvent(event)

    @Subscribe
    fun handleRevealOfAnswers(event: AnswersRevealedEvent) = handleEvent(event)

    @Subscribe
    fun handleReopenedQuestion(event: CurrentQuestionReopenedEvent) = handleEvent(event)

    @Subscribe
    fun handleQuizFinish(event: QuizFinishedEvent) {
        val quiz = quizCache[event.quizId]
        if (quiz.timestamp < event.timestamp) {
            quizCache.put(event.quizId, event.process(quiz))
        }

        quizStatisticsProvider.generateStatistics(event.quizId)
            .subscribe {
                quizCache.put(event.quizId, quizCache[event.quizId]!!.setQuizStatistics(it))
                emitQuiz(quizCache[event.quizId]!!)
            }
    }

    @Subscribe
    fun handleQuizDeletion(event: QuizDeletedEvent) {
        quizCache.invalidate(event.quizId)
        locks.remove(event.quizId)
        observables.remove(event.quizId)
    }

    @Subscribe
    fun handleForceEmitCommand(command: ForceEmitCommand) {
        eventRepository.determineEvents(command.quizId)
            .reduce(Quiz(name = "")) { q: Quiz, e: Event -> e.process(q) }
            .subscribe { emitQuiz(it) }
    }

    @Subscribe
    fun handleReloadQuizCommand(command: ReloadQuizCommand) {
        locks.computeIfAbsent(command.quizId) { Semaphore(1) }.acquire()
        try {
            eventRepository.determineEvents(command.quizId)
                .reduce(Quiz(name = "")) { q: Quiz, e: Event -> e.process(q) }
                .subscribe {
                    quizCache.put(command.quizId, it)
                    emitQuiz(it)
                }
        } finally {
            locks[command.quizId]!!.release()
        }
    }

    private fun handleEvent(event: Event) {
        logger.trace("handling event {}", event)
        try {
            locks.computeIfAbsent(event.quizId) { Semaphore(1) }.acquire()
            var quiz = quizCache[event.quizId]
            if (quiz.timestamp < event.timestamp) {
                quiz = event.process(quiz)
                quizCache.put(event.quizId, quiz)
            }
            emitQuiz(quiz)
        } finally {
            locks[event.quizId]!!.release()
        }
        logger.info("handled event {}", event)
    }

    override fun observeQuiz(quizId: UUID): Flux<Quiz> {
        return observables
            .computeIfAbsent(quizId) { Sinks.many().multicast().onBackpressureBuffer() }
            .asFlux()
    }

    private fun emitQuiz(quiz: Quiz) {
        observables
            .computeIfAbsent(quiz.id) { Sinks.many().multicast().onBackpressureBuffer() }
            .tryEmitNext(quiz.setRedoPossible(undoneEventsCache.isNotEmpty(quiz.id)))
    }

    fun removeObserver(quizId: UUID) {
        observables.remove(quizId)
    }

    override fun determineQuiz(quizId: UUID): Quiz? {
        return quizCache[quizId]
    }

}
