package team.undefined.quiz.core

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

@Component
class QuizStatisticsProjection(private val eventRepository: EventRepository,
                               private val eventBus: EventBus) {

    private val statistics = ConcurrentHashMap<UUID, QuizStatistics>()

    init {
        eventBus.register(this)
    }

    @Subscribe
    fun onQuestionAsked(event: QuestionAskedEvent) {
        val quizStatistics = statistics.computeIfAbsent(event.quizId) { QuizStatistics() }

        statistics[event.quizId] = quizStatistics.addQuestionStatistic(QuestionStatistics(event.questionId, event.timestamp))
    }

    @Subscribe
    fun onBuzzer(event: BuzzeredEvent) {
        val currentQuestion = statistics[event.quizId]!!.questionStatistics.last()
        val answerStatistic = AnswerStatistics(event.participantId, event.timestamp - currentQuestion.timestamp)
        currentQuestion.addAnswerStatistics(answerStatistic) // TODO: immutable
    }

    @Subscribe
    fun onEstimation(event: EstimatedEvent) {
        val currentQuestion = statistics[event.quizId]!!.questionStatistics.last()
        val answerStatistic = AnswerStatistics(event.participantId, event.timestamp - currentQuestion.timestamp, answer = event.estimatedValue)
        currentQuestion.addAnswerStatistics(answerStatistic) // TODO: immutable
    }

    @Subscribe
    fun onChoiceSelection(event: ChoiceSelectedEvent) {
        val currentQuestion = statistics[event.quizId]!!.questionStatistics.last()
        val answerStatistic = AnswerStatistics(event.participantId, event.timestamp - currentQuestion.timestamp, choiceId = event.choiceId)
        currentQuestion.addAnswerStatistics(answerStatistic) // TODO: immutable
    }

    @Subscribe
    fun onAnswer(event: AnsweredEvent) {
        statistics[event.quizId]!!.questionStatistics.last()
            .answerStatistics
            .findLast { it.participantId == event.participantId }
            ?.rating = event.answer
    }



    fun generateStatistics(quizId: UUID): Mono<QuizStatistics> {
        return eventRepository.determineEvents(quizId)
                .filter { it is QuestionAskedEvent || it is BuzzeredEvent || it is EstimatedEvent || it is ChoiceSelectedEvent || it is AnsweredEvent }
                .collectList()
                .map { createQuizStatistics(it) }
    }

    private fun createQuizStatistics(events: List<Event>): QuizStatistics {
        val questionStatistics = ArrayList<QuestionStatistics>()
        var currentQuestion: QuestionStatistics? = null
        var currentQuestionTimestamp: Long = 0

        val map: Multimap<UUID, AnswerStatisticsInformation> = ArrayListMultimap.create()

        for (event in events) {
            when (event) {
                is QuestionAskedEvent -> {
                    if (!map.isEmpty) {
                        map.forEach { participantId, answerStatisticsInformation ->
                            currentQuestion?.addAnswerStatistics(AnswerStatistics(participantId, answerStatisticsInformation.duration, answerStatisticsInformation.answer, answerStatisticsInformation.choiceId, answerStatisticsInformation.rating))
                        }
                        map.clear()
                    }
                    currentQuestion = QuestionStatistics(event.questionId, event.timestamp)
                    currentQuestionTimestamp = event.timestamp
                    questionStatistics.add(currentQuestion)
                }

                is BuzzeredEvent -> map.put(event.participantId, AnswerStatisticsInformation(event.timestamp - currentQuestionTimestamp))
                is EstimatedEvent -> map.put(event.participantId, AnswerStatisticsInformation(event.timestamp - currentQuestionTimestamp, event.estimatedValue))
                is ChoiceSelectedEvent -> map.put(event.participantId, AnswerStatisticsInformation(event.timestamp - currentQuestionTimestamp, choiceId = event.choiceId))
                is AnsweredEvent -> {
                    val lastPair = map.get(event.participantId).last()
                    lastPair.rating = event.answer
                }
            }
        }

        if (!map.isEmpty) {
            map.forEach { participantId, answerStatisticsInformation ->
                currentQuestion?.addAnswerStatistics(AnswerStatistics(participantId, answerStatisticsInformation.duration, answerStatisticsInformation.answer, answerStatisticsInformation.choiceId, answerStatisticsInformation.rating))
            }
            map.clear()
        }

        return QuizStatistics(questionStatistics)
    }

    fun determineQuizStatistics(quizId: UUID): QuizStatistics? {
        return statistics[quizId]
    }

}

private data class AnswerStatisticsInformation(
        val duration: Long,
        val answer: String? = null,
        val choiceId: UUID? = null,
        var rating: AnswerCommand.Answer = AnswerCommand.Answer.INCORRECT
)
