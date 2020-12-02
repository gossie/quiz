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
                .filter { it is QuestionAskedEvent || it is BuzzeredEvent || it is EstimatedEvent || it is ChoiceSelectedEvent || it is AnsweredEvent }
                .collectList()
                .map { createQuizStatistics(it) }
    }

    private fun createQuizStatistics(events: List<Event>): QuizStatistics {
        val questionStatistics = ArrayList<QuestionStatistics>();
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
                    currentQuestion = QuestionStatistics(event.questionId)
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

}

private data class AnswerStatisticsInformation(
        val duration: Long,
        val answer: String? = null,
        val choiceId: UUID? = null,
        var rating: AnswerCommand.Answer = AnswerCommand.Answer.INCORRECT
)
