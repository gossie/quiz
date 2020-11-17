package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import reactor.core.publisher.Flux
import java.util.concurrent.atomic.AtomicReference

internal class QuizProjectionQuizDeletedEventTest {

    @Test
    fun shouldHandleQuizDeletedEvent() {
        val quiz = Quiz(name = "Awesome Quiz")

        val question = Question(question = "Wofür steht die Abkürzung a.D.?")
        val participant = Participant(name = "Lena")

        val eventBus = EventBus()
        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, 1),
                        QuestionCreatedEvent(quiz.id, question, 2),
                        ParticipantCreatedEvent(quiz.id, participant, 3),
                        QuestionAskedEvent(quiz.id, question.id, 4),
                        BuzzeredEvent(quiz.id, participant.id, 5),
                        AnsweredEvent(quiz.id, participant.id, AnswerCommand.Answer.CORRECT, 6),
                        QuizFinishedEvent(quiz.id, 7)
                ))
        val quizProjection = QuizProjection(eventBus, QuizStatisticsProvider(eventRepository), eventRepository, UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(QuizCreatedEvent(quiz.id, quiz, 1))
        eventBus.post(QuestionCreatedEvent(quiz.id, question, 2))
        eventBus.post(ParticipantCreatedEvent(quiz.id, participant, 3))
        eventBus.post(QuestionAskedEvent(quiz.id, question.id, 4))
        eventBus.post(BuzzeredEvent(quiz.id, participant.id, 5))
        eventBus.post(AnsweredEvent(quiz.id, participant.id, AnswerCommand.Answer.CORRECT, 6))
        eventBus.post(QuizFinishedEvent(quiz.id, 7))

        await untilAsserted  {
            val q = observedQuiz.get()

            assertThat(q.id).isEqualTo(quiz.id)
            assertThat(q.finished).isTrue()
            assertThat(q.quizStatistics!!.questionStatistics).hasSize(1)
            assertThat(q.quizStatistics!!.questionStatistics[0].answerStatistics[0].duration).isEqualTo(1L)
            assertThat(q.quizStatistics!!.questionStatistics[0].answerStatistics[0].participantId).isEqualTo(participant.id)
            assertThat(q.quizStatistics!!.questionStatistics[0].answerStatistics[0].answer).isEqualTo(AnswerCommand.Answer.CORRECT)
        }

        eventBus.post(QuizDeletedEvent(quiz.id, 8))

        // TODO: await untilAsserted  { assertThat(observedQuiz.get()).isNull() }
    }

}