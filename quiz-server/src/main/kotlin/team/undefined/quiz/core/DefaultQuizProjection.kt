package team.undefined.quiz.core

import com.google.common.cache.CacheBuilder
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Semaphore

@Component
class DefaultQuizProjection(
    eventBus: EventBus,
    private val eventRepository: EventRepository,
    private val undoneEventsCache: UndoneEventsCache,
    quizProjectionConfiguration: QuizProjectionConfiguration
) : QuizProjection {

    private val logger = LoggerFactory.getLogger(QuizProjection::class.java)

    private val quizCache = CacheBuilder.newBuilder()
        .maximumSize(quizProjectionConfiguration.quizCacheMaxSize)
        .expireAfterAccess(Duration.ofHours(quizProjectionConfiguration.quizCacheDuration))
        .recordStats()
        .build<UUID, Quiz>()

    private val observables = ConcurrentHashMap<UUID, Sinks.Many<Quiz>>()
    private val locks = ConcurrentHashMap<UUID, Semaphore>()

    init {
        eventBus.register(this)
    }

    private fun determineQuizFromCacheOrDB(quizId: UUID): Mono<Quiz> {
        return Mono.justOrEmpty(quizCache.getIfPresent(quizId))
            .switchIfEmpty {
                logger.debug("Quiz $quizId not found in cache and is read from the database")
                eventRepository
                    .determineEvents(quizId)
                    .reduce(Quiz(name = "")) { quiz, event -> event.process(quiz) }
            }
    }

    @Subscribe
    fun handleQuizCreation(event: QuizCreatedEvent) {
        quizCache.put(event.quizId, event.process(event.quiz))
        determineQuizFromCacheOrDB(event.quizId)
            .subscribe {
                locks.computeIfAbsent(event.quizId) { Semaphore(1) }
                emitQuiz(it)
            }

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
        locks.computeIfAbsent(event.quizId) { Semaphore(1) }.acquire()
        determineQuizFromCacheOrDB(event.quizId)
            .doFinally { locks[event.quizId]!!.release() }
            .subscribe {
                var processedQuiz = it
                if (event.sequenceNumber > it.sequenceNumber) {
                    processedQuiz = event.process(it)
                    quizCache.put(event.quizId, processedQuiz)
                }
                emitQuiz(processedQuiz)
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
        eventRepository.determineEvents(command.quizId)
            .reduce(Quiz(name = "")) { q: Quiz, e: Event -> e.process(q) }
            .doFinally { locks[command.quizId]!!.release() }
            .subscribe {
                quizCache.put(command.quizId, it)
                emitQuiz(it)
            }
    }

    private fun handleEvent(event: Event) {
        logger.trace("handling event {}", event)
        locks.computeIfAbsent(event.quizId) { Semaphore(1) }.acquire()
        determineQuizFromCacheOrDB(event.quizId)
            .doFinally {
                locks[event.quizId]!!.release()
                logger.info("handled event {}", event)
            }
            .subscribe {
                var processedQuiz = it
                if (event.sequenceNumber > it.sequenceNumber) {
                    processedQuiz = event.process(it)
                    quizCache.put(event.quizId, processedQuiz)
                }
                emitQuiz(processedQuiz)
            }
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

    override fun determineQuiz(quizId: UUID): Mono<Quiz> {
        return determineQuizFromCacheOrDB(quizId)
    }

}
