package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import reactor.core.publisher.Flux
import java.util.concurrent.atomic.AtomicReference

internal class QuizProjectionParticipantCreatedEventTest {

    @Test
    fun shouldHandleParticipantCreationWhenQuizIsAlreadyInCache() {
        val quiz = Quiz(name = "Awesome Quiz")

        val eventBus = EventBus()
        val quizProjection = DefaultQuizProjection(eventBus, mock(EventRepository::class.java), UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        val participant = Participant(name = "Lena")
        eventBus.post(QuizCreatedEvent(quiz.id, quiz, sequenceNumber = 1))
        eventBus.post(ParticipantCreatedEvent(quiz.id, participant, sequenceNumber = 2))

        await untilAsserted {
            QuizAssert.assertThat(observedQuiz.get())
                    .hasId(quiz.id)
                    .particpantSizeIs(1)
                    .hasParticipant(0) { it.isEqualTo(participant) }
                    .hasNoQuestions()
                    .undoIsPossible()
                    .isNotFinished
        }
    }

    @Test
    fun shouldHandleParticipantCreationWhenQuizIsNotInCacheAndLastEventWasAlreadyPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")

        val participant = Participant(name = "Lena")
        val participantCreatedEvent = ParticipantCreatedEvent(quiz.id, participant, sequenceNumber = 2)

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(QuizCreatedEvent(quiz.id, quiz, sequenceNumber = 1), participantCreatedEvent))

        val eventBus = EventBus()
        val quizProjection = DefaultQuizProjection(eventBus, eventRepository, UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(participantCreatedEvent)

        await untilAsserted {
            QuizAssert.assertThat(observedQuiz.get())
                    .hasId(quiz.id)
                    .particpantSizeIs(1)
                    .hasParticipant(0) { it.isEqualTo(participant) }
                    .hasNoQuestions()
                    .undoIsPossible()
                    .isNotFinished
        }
    }

    @Test
    fun shouldHandleParticipantCreationWhenQuizIsNotInCacheAndLastEventWasNotYetPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(QuizCreatedEvent(quiz.id, quiz, sequenceNumber = 1)))

        val eventBus = EventBus()
        val quizProjection = DefaultQuizProjection(eventBus, eventRepository, UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        val participant = Participant(name = "Lena")
        eventBus.post(ParticipantCreatedEvent(quiz.id, participant, sequenceNumber = 2))

        await untilAsserted {
            QuizAssert.assertThat(observedQuiz.get())
                    .hasId(quiz.id)
                    .particpantSizeIs(1)
                    .hasParticipant(0) { it.isEqualTo(participant) }
                    .hasNoQuestions()
                    .undoIsPossible()
                    .isNotFinished
        }
    }

}