package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Disabled
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
        val quizProjection = DefaultQuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), mock(EventRepository::class.java), UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        val question1 = Question(question = "Wofür steht die Abkürzung a.D.?", category = QuestionCategory("Erdkunde"))
        val question2 = Question(question = "Wer schrieb Peter und der Wolf?", category = QuestionCategory("Literatur"))
        eventBus.post(QuizCreatedEvent(quiz.id, quiz, 1))
        eventBus.post(QuestionCreatedEvent(quiz.id, question1, 2))
        eventBus.post(QuestionCreatedEvent(quiz.id, question2, 3))

        await untilAsserted {
            assertThat(observedQuiz.get())
                    .hasId(quiz.id)
                    .hasNoParticipants()
                    .questionSizeIs(2)
                    .hasQuestion(0) { it.isEqualTo(question1) }
                    .hasQuestion(1) {
                        it
                                .hasId(question2.id)
                                .hasQuestion("Wer schrieb Peter und der Wolf?")
                                .hasPreviousQuestionId(question1.id)
                    }
                    .undoIsPossible()
                    .isNotFinished
        }
    }

    @Test
    @Disabled
    fun shouldHandleQuestionCreationWhenQuizIsNotInCacheAndLastEventWasAlreadyPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")

        val question1 = Question(question = "Wofür steht die Abkürzung a.D.?")
        val question2 = Question(question = "Wer schrieb Peter und der Wolf?", category = QuestionCategory("Literatur"))
        val question2CreatedEvent = QuestionCreatedEvent(quiz.id, question1, 3)

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, 1),
                        QuestionCreatedEvent(quiz.id, question1, 2),
                        question2CreatedEvent
                ))

        val eventBus = EventBus()
        val quizProjection = DefaultQuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), eventRepository, UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(question2CreatedEvent)

        await untilAsserted {
            assertThat(observedQuiz.get())
                    .hasId(quiz.id)
                    .hasNoParticipants()
                    .questionSizeIs(2)
                    .hasQuestion(0) { it.isEqualTo(question1) }
                    .hasQuestion(1) {
                        it
                                .hasId(question2.id)
                                .hasQuestion("Wer schrieb Peter und der Wolf?")
                                .hasPreviousQuestionId(question1.id)
                    }
                    .undoIsPossible()
                    .isNotFinished
        }
    }

    @Test
    fun shouldHandleQuestionCreationWhenQuizIsNotInCacheAndLastEventWasNotYetPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")

        val question1 = Question(question = "Wofür steht die Abkürzung a.D.?")

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, 1),
                        QuestionCreatedEvent(quiz.id, question1, 2)
                ))

        val eventBus = EventBus()
        val quizProjection = DefaultQuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), eventRepository, UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }


        val question2 = Question(question = "Wer schrieb Peter und der Wolf?", category = QuestionCategory("Literatur"))
        eventBus.post(QuestionCreatedEvent(quiz.id, question2, 3))

        await untilAsserted {
            assertThat(observedQuiz.get())
                    .hasId(quiz.id)
                    .hasNoParticipants()
                    .questionSizeIs(2)
                    .hasQuestion(0) { it.isEqualTo(question1) }
                    .hasQuestion(1) {
                        it
                                .hasId(question2.id)
                                .hasQuestion("Wer schrieb Peter und der Wolf?")
                                .hasPreviousQuestionId(question1.id)
                    }
                    .undoIsPossible()
                    .isNotFinished
        }
    }

}