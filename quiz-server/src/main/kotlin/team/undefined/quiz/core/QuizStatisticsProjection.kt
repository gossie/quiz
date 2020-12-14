package team.undefined.quiz.core

import com.google.common.cache.CacheBuilder
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.Duration
import java.util.*

@Component
class QuizStatisticsProjection(
    private val eventRepository: EventRepository,
    eventBus: EventBus,
    quizStatisticsProjectionConfiguration: QuizStatisticsProjectionConfiguration
) {

    private val logger = LoggerFactory.getLogger(QuizStatisticsProjection::class.java)

    private val statistics = CacheBuilder.newBuilder()
        .maximumSize(quizStatisticsProjectionConfiguration.quizStatisticsCacheMaxSize)
        .expireAfterAccess(Duration.ofHours(quizStatisticsProjectionConfiguration.quizStatisticsCacheDuration))
        .recordStats()
        .build<UUID, QuizStatistics>()

    init {
        eventBus.register(this)
    }

    private fun determineQuizStatisticsFromCacheOrDB(quizId: UUID): Mono<QuizStatistics> {
        return Mono.justOrEmpty(statistics.getIfPresent(quizId))
            .doOnNext { logger.debug("${javaClass.name} - QuizStatistics for quiz $quizId found in cache") }
            .switchIfEmpty {
                logger.debug("QuizStatistics for quiz $quizId not found in cache and is read from the database")
                eventRepository.determineEvents(quizId)
                    .reduce(QuizStatistics()) { quizStatistics, event ->
                        when (event) {
                            is QuestionAskedEvent -> quizStatistics.addQuestionStatistic(QuestionStatistics(event.questionId, event.timestamp))
                            is BuzzeredEvent -> addAnswerStatistics(quizStatistics, event.participantId, event.timestamp)
                            is EstimatedEvent -> addAnswerStatistics(quizStatistics, event.participantId, event.timestamp, event.estimatedValue)
                            is ChoiceSelectedEvent -> addAnswerStatistics(quizStatistics, event.participantId, event.timestamp, choiceId = event.choiceId)
                            is AnsweredEvent -> handleAnswer(quizStatistics, event.participantId, event.answer)
                            else -> quizStatistics
                        }
                    }
            }
    }

    private fun addAnswerStatistics(quizStatistics: QuizStatistics, participantId: UUID, timestamp: Long, answer: String? = null, choiceId: UUID? = null): QuizStatistics {
        val currentQuestion = quizStatistics.questionStatistics.lastOrNull()
        currentQuestion?.addAnswerStatistics(AnswerStatistics(participantId, timestamp - currentQuestion.timestamp, answer, choiceId)) // TODO: immutable
        return quizStatistics
    }

    private fun handleAnswer(quizStatistics: QuizStatistics, participantId: UUID, rating: AnswerCommand.Answer): QuizStatistics {
        quizStatistics.questionStatistics.last()
            .answerStatistics
            .findLast { it.participantId == participantId }
            ?.rating = rating
        return quizStatistics
    }

    @Subscribe
    fun onQuizCreation(event: QuizCreatedEvent) {
        statistics.put(event.quizId, QuizStatistics())
        logger.info("handled QuizCreatedEvent")
    }

    @Subscribe
    fun onQuestionAsked(event: QuestionAskedEvent) {
        determineQuizStatisticsFromCacheOrDB(event.quizId)
            .subscribe {
                statistics.put(event.quizId, it.addQuestionStatistic(QuestionStatistics(event.questionId, event.timestamp)))
                logger.info("handled QuestionAskedEvent")
            }
    }

    @Subscribe
    fun onBuzzer(event: BuzzeredEvent) {
        determineQuizStatisticsFromCacheOrDB(event.quizId)
            .map { addAnswerStatistics(it, event.participantId, event.timestamp) }
            .subscribe {
                statistics.put(event.quizId, it)
                logger.info("handled BuzzeredEvent")
            }
    }

    @Subscribe
    fun onEstimation(event: EstimatedEvent) {
        determineQuizStatisticsFromCacheOrDB(event.quizId)
            .map { addAnswerStatistics(it, event.participantId, event.timestamp, event.estimatedValue) }
            .subscribe {
                statistics.put(event.quizId, it)
                logger.info("handled EstimatedEvent")
            }
    }

    @Subscribe
    fun onChoiceSelection(event: ChoiceSelectedEvent) {
        determineQuizStatisticsFromCacheOrDB(event.quizId)
            .map { addAnswerStatistics(it, event.participantId, event.timestamp, choiceId = event.choiceId) }
            .subscribe {
                statistics.put(event.quizId, it)
                logger.info("handled ChoiceSelectedEvent")
            }
    }

    @Subscribe
    fun onAnswer(event: AnsweredEvent) {
        determineQuizStatisticsFromCacheOrDB(event.quizId)
            .map { handleAnswer(it, event.participantId, event.answer) }
            .subscribe {
                statistics.put(event.quizId, it)
                logger.info("handled AnsweredEvent")
            }
    }

    fun determineQuizStatistics(quizId: UUID): Mono<QuizStatistics> {
        return determineQuizStatisticsFromCacheOrDB(quizId)
    }

}
