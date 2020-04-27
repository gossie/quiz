package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import reactor.core.publisher.Flux
import java.util.concurrent.atomic.AtomicReference

internal class QuizProjectionQuestionCreatedEventTest {

    @Test
    fun shouldHandleQuestionCreationWhenQuizIsAlreadyInCache() {
        val quiz = Quiz(name = "Awesome Quiz")

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), mock(EventRepository::class.java))

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        val question = Question(question = "Wofür steht die Abkürzung a.D.?")
        eventBus.post(QuizCreatedEvent(quiz.id, quiz, 1))
        eventBus.post(QuestionCreatedEvent(quiz.id, question, 2))

        await until {
            observedQuiz.get().id == quiz.id
                    && observedQuiz.get().participants.isEmpty()
                    && observedQuiz.get().questions.size == 1
                    && observedQuiz.get().questions.contains(question)
                    && !observedQuiz.get().finished
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
        val quizProjection = QuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), eventRepository)

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(questionCreatedEvent)

        await until {
            observedQuiz.get().id == quiz.id
                    && observedQuiz.get().participants.isEmpty()
                    && observedQuiz.get().questions.size == 1
                    && observedQuiz.get().questions.contains(question)
                    && !observedQuiz.get().finished
        }
    }

    @Test
    fun shouldHandleQuestionCreationWhenQuizIsNotInCacheAndLastEventWasNotYetPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(QuizCreatedEvent(quiz.id, quiz, 1)))

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), eventRepository)

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        val question = Question(question = "Wofür steht die Abkürzung a.D.?")
        eventBus.post(QuestionCreatedEvent(quiz.id, question, 2))

        await until {
            observedQuiz.get().id == quiz.id
                    && observedQuiz.get().participants.isEmpty()
                    && observedQuiz.get().questions.size == 1
                    && observedQuiz.get().questions.contains(question)
                    && !observedQuiz.get().finished
        }
    }

}