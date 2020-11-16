package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import reactor.core.publisher.Flux
import team.undefined.quiz.core.QuizAssert.assertThat
import java.util.concurrent.atomic.AtomicReference

internal class QuizProjectionQuestionRevertedEventTest {

    @Test
    fun shouldHandleQuestionRevertedEventWhenQuizIsAlreadyInCache_askQuestion() {
        val quiz = Quiz(name = "Awesome Quiz")

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), mock(EventRepository::class.java), UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        val question = Question(question = "Wofür steht die Abkürzung a.D.?", initialTimeToAnswer = 5, estimates = HashMap())
        eventBus.post(QuizCreatedEvent(quiz.id, quiz, 1))
        eventBus.post(QuestionCreatedEvent(quiz.id, question, 2))
        eventBus.post(ParticipantCreatedEvent(quiz.id, Participant(name = "Lena"), 3))
        eventBus.post(QuestionAskedEvent(quiz.id, question.id, 4))
        eventBus.post(TimeToAnswerDecreasedEvent(quiz.id, question.id, 5))
        eventBus.post(QuestionAskedEvent(quiz.id, question.id, 6))

        await untilAsserted {
            assertThat(observedQuiz.get())
                    .hasId(quiz.id)
                    .particpantSizeIs(1)
                    .questionSizeIs(1)
                    .hasQuestion(0) { question ->
                        question
                                .isNotPending
                                .initialTimeToAnswerIs(5)
                                .secondsLeftIs(5)
                    }
                    .undoIsPossible()
                    .isNotFinished
        }
    }

    @Test
    fun shouldHandleQuestionRevertedEventWhenQuizIsNotInCacheAndLastEventWasAlreadyPersisted_askQuestion() {
        val quiz = Quiz(name = "Awesome Quiz")

        val question = Question(question = "Wofür steht die Abkürzung a.D.?", initialTimeToAnswer = 5, estimates = HashMap())
        val questionRevertedEvent = QuestionAskedEvent(quiz.id, question.id, 6)

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, 1),
                        QuestionCreatedEvent(quiz.id, question, 2),
                        ParticipantCreatedEvent(quiz.id, Participant(name = "Lena"), 3),
                        QuestionAskedEvent(quiz.id, question.id, 4),
                        TimeToAnswerDecreasedEvent(quiz.id, question.id, 5),
                        questionRevertedEvent
                ))

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), eventRepository, UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(questionRevertedEvent)

        await untilAsserted {
            assertThat(observedQuiz.get())
                    .hasId(quiz.id)
                    .particpantSizeIs(1)
                    .questionSizeIs(1)
                    .hasQuestion(0) { question ->
                        question
                                .isNotPending
                                .initialTimeToAnswerIs(5)
                                .secondsLeftIs(5)
                    }
                    .undoIsPossible()
                    .isNotFinished
        }
    }

    @Test
    fun shouldHandleQuestionRevertedEventCreationWhenQuizIsNotInCacheAndLastEventWasNotYetPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")
        val question = Question(question = "Wofür steht die Abkürzung a.D.?", initialTimeToAnswer = 5, estimates = HashMap())

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, 1),
                        QuestionCreatedEvent(quiz.id, question, 2),
                        ParticipantCreatedEvent(quiz.id, Participant(name = "Lena"), 3),
                        QuestionAskedEvent(quiz.id, question.id, 4),
                        TimeToAnswerDecreasedEvent(quiz.id, question.id, 5)
                ))

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), eventRepository, UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(QuestionAskedEvent(quiz.id, question.id, 6))

        await untilAsserted {
            assertThat(observedQuiz.get())
                    .hasId(quiz.id)
                    .particpantSizeIs(1)
                    .questionSizeIs(1)
                    .hasQuestion(0) { question ->
                        question
                                .isNotPending
                                .initialTimeToAnswerIs(5)
                                .secondsLeftIs(5)
                    }
                    .undoIsPossible()
                    .isNotFinished
        }
    }

}