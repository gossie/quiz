package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import reactor.core.publisher.Flux
import java.util.concurrent.atomic.AtomicReference

internal class QuizProjectionBuzzeredEventTest {

    @Test
    fun shouldHandleBuzzeredEventWhenQuizIsAlreadyInCache() {
        val quiz = Quiz(name = "Awesome Quiz")

        val eventBus = EventBus()
        val quizProjection = DefaultQuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), mock(EventRepository::class.java), UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        val question = Question(question = "Wofür steht die Abkürzung a.D.?")
        val participant = Participant(name = "Lena")
        eventBus.post(QuizCreatedEvent(quiz.id, quiz, 1))
        eventBus.post(QuestionCreatedEvent(quiz.id, question, 2))
        eventBus.post(ParticipantCreatedEvent(quiz.id, participant, 3))
        eventBus.post(QuestionAskedEvent(quiz.id, question.id, 4))
        eventBus.post(BuzzeredEvent(quiz.id, participant.id, 5))

        await until {
            observedQuiz.get().id == quiz.id
                    && observedQuiz.get().participants.size == 1
                    && observedQuiz.get().participants[0].turn
                    && observedQuiz.get().questions.size == 1
                    && observedQuiz.get().questions[0].pending
                    && observedQuiz.get().undoPossible
                    && !observedQuiz.get().finished
        }
    }

    @Test
    fun shouldHandleBuzzeredEventWhenQuizIsNotInCacheAndLastEventWasAlreadyPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")

        val question = Question(question = "Wofür steht die Abkürzung a.D.?")
        val participant = Participant(name = "Lena")
        val buzzeredEvent = BuzzeredEvent(quiz.id, participant.id, 5)

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, 1),
                        QuestionCreatedEvent(quiz.id, question, 2),
                        ParticipantCreatedEvent(quiz.id, participant, 3),
                        QuestionAskedEvent(quiz.id, question.id, 4),
                        buzzeredEvent
                ))

        val eventBus = EventBus()
        val quizProjection = DefaultQuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), eventRepository, UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(buzzeredEvent)

        await until {
            observedQuiz.get().id == quiz.id
                    && observedQuiz.get().participants.size == 1
                    && observedQuiz.get().participants[0].turn
                    && observedQuiz.get().questions.size == 1
                    && observedQuiz.get().questions[0].pending
                    && observedQuiz.get().undoPossible
                    && !observedQuiz.get().finished
        }
    }

    @Test
    fun shouldHandleBuzzeredEventCreationWhenQuizIsNotInCacheAndLastEventWasNotYetPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")
        val question = Question(question = "Wofür steht die Abkürzung a.D.?")
        val participant = Participant(name = "Lena")

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, 1),
                        QuestionCreatedEvent(quiz.id, question, 2),
                        ParticipantCreatedEvent(quiz.id, participant, 3),
                        QuestionAskedEvent(quiz.id, question.id, 4)
                ))

        val eventBus = EventBus()
        val quizProjection = DefaultQuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), eventRepository, UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(BuzzeredEvent(quiz.id, participant.id, 5))

        await until {
            observedQuiz.get().id == quiz.id
                    && observedQuiz.get().participants.size == 1
                    && observedQuiz.get().participants[0].turn
                    && observedQuiz.get().questions.size == 1
                    && observedQuiz.get().questions[0].pending
                    && observedQuiz.get().undoPossible
                    && !observedQuiz.get().finished
        }
    }

}