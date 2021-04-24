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
                            is ParticipantCreatedEvent -> quizStatistics.addParticipantStatistics(ParticipantStatistics(event.participant.id))
                            is ParticipantDeletedEvent -> quizStatistics.deleteParticipantStatistics(event.participantId)
                            is QuestionAskedEvent -> quizStatistics.addQuestionStatistic(QuestionStatistics(event.questionId))
                            is AnsweredEvent -> quizStatistics.handleAnswer(event.participantId, event.answer)
                            else -> quizStatistics
                        }
                    }
                    .doOnNext { statistics.put(quizId, it) }
            }
    }

    @Subscribe
    fun onQuizCreation(event: QuizCreatedEvent) {
        statistics.put(event.quizId, QuizStatistics())
        logger.info("handled QuizCreatedEvent")
    }

    @Subscribe
    fun onParticiantCreation(event: ParticipantCreatedEvent) {
        determineQuizStatisticsFromCacheOrDB(event.quizId)
                .subscribe {
                    statistics.put(event.quizId, it.addParticipantStatistics(ParticipantStatistics(event.participant.id)))
                    logger.info("handled ParticipantCreatedEvent")
                }
    }

    @Subscribe
    fun onParticiantDeletion(event: ParticipantDeletedEvent) {
        determineQuizStatisticsFromCacheOrDB(event.quizId)
                .subscribe {
                    statistics.put(event.quizId, it.deleteParticipantStatistics(event.participantId))
                    logger.info("handled ParticipantCreatedEvent")
                }
    }

    @Subscribe
    fun onQuestionAsked(event: QuestionAskedEvent) {
        determineQuizStatisticsFromCacheOrDB(event.quizId)
            .subscribe {
                statistics.put(event.quizId, it.addQuestionStatistic(QuestionStatistics(event.questionId)))
                logger.info("handled QuestionAskedEvent")
            }
    }

    @Subscribe
    fun onAnswer(event: AnsweredEvent) {
        determineQuizStatisticsFromCacheOrDB(event.quizId)
            .subscribe {
                statistics.put(event.quizId, it.handleAnswer(event.participantId, event.answer))
                logger.info("handled AnsweredEvent")
            }
    }

    fun determineQuizStatistics(quizId: UUID): Mono<QuizStatistics> {
        return determineQuizStatisticsFromCacheOrDB(quizId)
    }

}
