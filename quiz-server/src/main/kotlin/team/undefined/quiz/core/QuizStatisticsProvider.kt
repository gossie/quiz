package team.undefined.quiz.core

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*
import kotlin.collections.ArrayList

@Component
class QuizStatisticsProvider(private val eventRepository: EventRepository) {

    fun generateStatistics(quizId: UUID): Mono<QuizStatistics> {
        return eventRepository.determineEvents(quizId)
                .filter { it is QuestionAskedEvent || it is BuzzeredEvent || it is EstimatedEvent || it is AnsweredEvent }
                .collectList()
                .map { createQuizStatistics(it) }
    }

    private fun createQuizStatistics(events: List<Event>): QuizStatistics {
        val questionStatistics = ArrayList<QuestionStatistics>();
        var currentQuestion: QuestionStatistics? = null
        var currentQuestionTimestamp: Long = 0

        val map: Multimap<UUID, Pair<Long, AnswerCommand.Answer>> = ArrayListMultimap.create()

        for (event in events) {
            when (event) {
                is QuestionAskedEvent -> {
                    if (!map.isEmpty) {
                        map.forEach { participantId, pair ->
                            currentQuestion?.addAnswerStatistics(AnswerStatistics(participantId, pair.first, rating = pair.second))
                        }
                        map.clear()
                    }
                    currentQuestion = QuestionStatistics(event.questionId)
                    currentQuestionTimestamp = event.timestamp
                    questionStatistics.add(currentQuestion)
                }

                is BuzzeredEvent -> map.put(event.participantId, Pair(event.timestamp - currentQuestionTimestamp, AnswerCommand.Answer.INCORRECT))
                is EstimatedEvent -> map.put(event.participantId, Pair(event.timestamp - currentQuestionTimestamp, AnswerCommand.Answer.INCORRECT))
                is AnsweredEvent -> {
                    val lastPair = map.get(event.participantId).last()
                    map.get(event.participantId).remove(lastPair)
                    map.put(event.participantId, Pair(lastPair.first ?: 0L, event.answer))
                }
            }
        }

        if (!map.isEmpty) {
            map.forEach { participantId, pair ->
                currentQuestion?.addAnswerStatistics(AnswerStatistics(participantId, pair.first, rating = pair.second))
            }
            map.clear()
        }

        return QuizStatistics(questionStatistics)
    }
}