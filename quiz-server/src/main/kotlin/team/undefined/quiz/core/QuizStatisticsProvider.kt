package team.undefined.quiz.core

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

        val map = LinkedHashMap<UUID, Pair<Long, AnswerCommand.Answer>>()

        for (event in events) {
            when (event) {
                is QuestionAskedEvent -> {
                    if (map.isNotEmpty()) {
                        map.forEach { (t, u) ->
                            currentQuestion?.addBuzzerStatistics(BuzzerStatistics(t, u.first, u.second))
                        }
                        map.clear()
                    }
                    currentQuestion = QuestionStatistics(event.questionId)
                    currentQuestionTimestamp = event.timestamp
                    questionStatistics.add(currentQuestion)
                }

                is BuzzeredEvent -> map[event.participantId] = Pair(event.timestamp - currentQuestionTimestamp, AnswerCommand.Answer.INCORRECT)
                is EstimatedEvent -> map[event.participantId] = Pair(event.timestamp - currentQuestionTimestamp, AnswerCommand.Answer.INCORRECT)
                is AnsweredEvent -> map[event.participantId] = Pair(map[event.participantId]?.first ?: 0L, event.answer)
            }
        }

        if (map.isNotEmpty()) {
            map.forEach { (t, u) ->
                currentQuestion!!.addBuzzerStatistics(BuzzerStatistics(t, u.first, u.second))
            }
            map.clear()
        }

        return QuizStatistics(questionStatistics)
    }
}