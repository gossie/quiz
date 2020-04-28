package team.undefined.quiz.core

import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*
import kotlin.collections.ArrayList

@Component
class QuizStatisticsProvider(private val eventRepository: EventRepository) {

    fun generateStatistics(quizId: UUID): Mono<QuizStatistics> {
        return eventRepository.determineEvents(quizId)
                .filter { it is QuestionAskedEvent || it is BuzzeredEvent || it is AnsweredEvent }
                .collectList()
                .map { createQuizStatistics(it) }
    }

    private fun createQuizStatistics(events: List<Event>): QuizStatistics {
        val questionStatistics = ArrayList<QuestionStatistics>();
        var currentQuestion: QuestionStatistics? = null
        var currentQuestionTimestamp: Long = 0
        var currentParticipantId: UUID? = null
        var buzzerDuration: Long = 0

        for (event in events) {
            if (event is QuestionAskedEvent) {
                currentQuestion = QuestionStatistics(event.questionId)
                currentQuestionTimestamp = event.timestamp
                questionStatistics.add(currentQuestion)
            } else if (event is BuzzeredEvent) {
                currentParticipantId = event.participantId
                buzzerDuration = event.timestamp - currentQuestionTimestamp
            } else if (event is AnsweredEvent) {
                currentQuestion!!.addBuzzerStatistics(BuzzerStatistics(currentParticipantId!!, buzzerDuration, event.answer))
            }
        }
        return QuizStatistics(questionStatistics)
    }
}