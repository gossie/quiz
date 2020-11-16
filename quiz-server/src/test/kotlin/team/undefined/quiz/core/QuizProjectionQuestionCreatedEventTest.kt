package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import org.assertj.core.api.Assertions.*
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import reactor.core.publisher.Flux
import team.undefined.quiz.core.QuizAssert.assertThat
import java.util.concurrent.atomic.AtomicReference

internal class QuizProjectionQuestionCreatedEventTest {

    @Test
    fun shouldHandleQuestionCreationWhenQuizIsAlreadyInCache() {
        val quiz = Quiz(name = "Awesome Quiz")

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), mock(EventRepository::class.java), UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        val question = Question(question = "Wofür steht die Abkürzung a.D.?", category = QuestionCategory("Erdkunde"))
        eventBus.post(QuizCreatedEvent(quiz.id, quiz, 1))
        eventBus.post(QuestionCreatedEvent(quiz.id, question, 2))

        await untilAsserted {
            assertThat(observedQuiz.get())
                    .hasId(quiz.id)
                    .hasNoParticipants()
                    .questionSizeIs(1)
                    .hasQuestion(0) { it.isEqualTo(question) }
                    .undoIsPossible()
                    .isNotFinished
        }
    }

    @Test
    fun shouldHandleQuestionCreationWhenQuizIsNotInCacheAndLastEventWasAlreadyPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")

        val question = Question(question = "Wofür steht die Abkürzung a.D.?")
        val questionCreatedEvent = QuestionCreatedEvent(quiz.id, question, 2)

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(QuizCreatedEvent(quiz.id, quiz, 1), questionCreatedEvent))

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), eventRepository, UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(questionCreatedEvent)

        await untilAsserted {
            assertThat(observedQuiz.get())
                    .hasId(quiz.id)
                    .hasNoParticipants()
                    .questionSizeIs(1)
                    .hasQuestion(0) { it.isEqualTo(question) }
                    .undoIsPossible()
                    .isNotFinished
        }
    }

    @Test
    fun shouldHandleQuestionCreationWhenQuizIsNotInCacheAndLastEventWasNotYetPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(QuizCreatedEvent(quiz.id, quiz, 1)))

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), eventRepository, UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        val question = Question(question = "Wofür steht die Abkürzung a.D.?")
        eventBus.post(QuestionCreatedEvent(quiz.id, question, 2))

        await untilAsserted {
            assertThat(observedQuiz.get())
                    .hasId(quiz.id)
                    .hasNoParticipants()
                    .questionSizeIs(1)
                    .hasQuestion(0) { it.isEqualTo(question) }
                    .undoIsPossible()
                    .isNotFinished
        }
    }

}