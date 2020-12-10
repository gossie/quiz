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

internal class QuizProjectionEstimatedEventTest {

    @Test
    fun shouldHandleEstimatedEventWhenQuizIsAlreadyInCache() {
        val quiz = Quiz(name = "Awesome Quiz")

        val eventBus = EventBus()
        val quizProjection = DefaultQuizProjection(eventBus, mock(EventRepository::class.java), UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        val question = Question(question = "Wofür steht die Abkürzung a.D.?", estimates = HashMap())
        val participant = Participant(name = "Lena")
        eventBus.post(QuizCreatedEvent(quiz.id, quiz, 1))
        eventBus.post(QuestionCreatedEvent(quiz.id, question, 2))
        eventBus.post(ParticipantCreatedEvent(quiz.id, participant, 3))
        eventBus.post(QuestionAskedEvent(quiz.id, question.id, 4))
        eventBus.post(EstimatedEvent(quiz.id, participant.id, "myEstimatedValue", 5))

        await untilAsserted {
            val q = observedQuiz.get()

            assertThat(q.id).isEqualTo(quiz.id)
            assertThat(q.participants).hasSize(1)
            assertThat(q.questions).hasSize(1)
            assertThat(q.questions[0].pending).isTrue()
            assertThat(q.questions[0].estimates).hasSize(1)
            assertThat(q.questions[0].estimates!![participant.id]).isEqualTo("myEstimatedValue")
            assertThat(q.undoPossible).isTrue()
            assertThat(q.finished).isFalse()
        }
    }

    @Test
    fun shouldHandleEstimatedEventWhenQuizIsNotInCacheAndLastEventWasAlreadyPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")

        val question = Question(question = "Wofür steht die Abkürzung a.D.?", estimates = HashMap())
        val participant = Participant(name = "Lena")
        val estimatedEvent = EstimatedEvent(quiz.id, participant.id, "myEstimatedValue", 5)

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, 1),
                        QuestionCreatedEvent(quiz.id, question, 2),
                        ParticipantCreatedEvent(quiz.id, participant, 3),
                        QuestionAskedEvent(quiz.id, question.id, 4),
                        estimatedEvent
                ))

        val eventBus = EventBus()
        val quizProjection = DefaultQuizProjection(eventBus, eventRepository, UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(estimatedEvent)

        await untilAsserted {
            val q = observedQuiz.get()

            assertThat(q.id).isEqualTo(quiz.id)
            assertThat(q.participants).hasSize(1)
            assertThat(q.questions).hasSize(1)
            assertThat(q.questions[0].pending).isTrue()
            assertThat(q.questions[0].estimates).hasSize(1)
            assertThat(q.questions[0].estimates!![participant.id]).isEqualTo("myEstimatedValue")
            assertThat(q.undoPossible).isTrue()
            assertThat(q.finished).isFalse()
        }
    }

    @Test
    fun shouldHandleEstimatedEventCreationWhenQuizIsNotInCacheAndLastEventWasNotYetPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")
        val question = Question(question = "Wofür steht die Abkürzung a.D.?", estimates = HashMap())
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
        val quizProjection = DefaultQuizProjection(eventBus, eventRepository, UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(EstimatedEvent(quiz.id, participant.id, "myEstimatedValue", 5))

        await untilAsserted {
            val q = observedQuiz.get()

            assertThat(q.id).isEqualTo(quiz.id)
            assertThat(q.participants).hasSize(1)
            assertThat(q.questions).hasSize(1)
            assertThat(q.questions[0].pending).isTrue()
            assertThat(q.questions[0].estimates).hasSize(1)
            assertThat(q.questions[0].estimates!![participant.id]).isEqualTo("myEstimatedValue")
            assertThat(q.undoPossible).isTrue()
            assertThat(q.finished).isFalse()
        }
    }

}