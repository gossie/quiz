package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import reactor.core.publisher.Flux
import java.util.concurrent.atomic.AtomicReference

internal class QuizProjectionQuestionDeletedEventTest {

    @Test
    fun shouldHandleQuestionDeletedEventWhenQuizIsAlreadyInCache() {
        val quiz = Quiz(name = "Awesome Quiz")

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), mock(EventRepository::class.java), UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        val question = Question(question = "Wofür steht die Abkürzung a.D.?")
        eventBus.post(QuizCreatedEvent(quiz.id, quiz, 1))
        eventBus.post(QuestionCreatedEvent(quiz.id, question, 2))
        eventBus.post(ParticipantCreatedEvent(quiz.id, Participant(name = "Lena"), 3))
        eventBus.post(QuestionDeletedEvent(quiz.id, question.id, 4))

        await until {
            observedQuiz.get().id == quiz.id
                    && observedQuiz.get().participants.size == 1
                    && observedQuiz.get().questions.isEmpty()
                    && observedQuiz.get().isUndoPossible()
                    && !observedQuiz.get().finished
        }
    }

    @Test
    fun shouldHandleQuestionDeletedEventWhenQuizIsNotInCacheAndLastEventWasAlreadyPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")

        val question = Question(question = "Wofür steht die Abkürzung a.D.?")
        val questionDeletedEvent = QuestionDeletedEvent(quiz.id, question.id, 4)

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, 1),
                        QuestionCreatedEvent(quiz.id, question, 2),
                        ParticipantCreatedEvent(quiz.id, Participant(name = "Lena"), 3),
                        questionDeletedEvent
                ))

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), eventRepository, UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(questionDeletedEvent)

        await until {
            observedQuiz.get().id == quiz.id
                    && observedQuiz.get().participants.size == 1
                    && observedQuiz.get().questions.isEmpty()
                    && observedQuiz.get().isUndoPossible()
                    && !observedQuiz.get().finished
        }
    }

    @Test
    fun shouldHandleQuestionDeletedEventCreationWhenQuizIsNotInCacheAndLastEventWasNotYetPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")
        val question = Question(question = "Wofür steht die Abkürzung a.D.?")

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, 1),
                        QuestionCreatedEvent(quiz.id, question, 2),
                        ParticipantCreatedEvent(quiz.id, Participant(name = "Lena"), 3)
                ))

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), eventRepository, UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(QuestionDeletedEvent(quiz.id, question.id, 4))

        await until {
            observedQuiz.get().id == quiz.id
                    && observedQuiz.get().participants.size == 1
                    && observedQuiz.get().questions.isEmpty()
                    && observedQuiz.get().isUndoPossible()
                    && !observedQuiz.get().finished
        }
    }

}