package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import team.undefined.quiz.core.QuizAssert.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import reactor.core.publisher.Flux
import java.util.concurrent.atomic.AtomicReference

internal class QuizProjectionParticipantDeletedEventTest {

    @Test
    fun shouldHandleParticipantDeletionWhenQuizIsAlreadyInCache() {
        val quiz = Quiz(name = "Awesome Quiz")

        val eventBus = EventBus()
        val quizProjection = DefaultQuizProjection(
            eventBus,
            mock(EventRepository::class.java),
            UndoneEventsCache(),
            QuizProjectionConfiguration(25, 1)
        )

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        val participant = Participant(name = "Lena")
        eventBus.post(QuizCreatedEvent(quiz.id, quiz, sequenceNumber = 1))
        eventBus.post(ParticipantCreatedEvent(quiz.id, participant, sequenceNumber = 2))
        eventBus.post(ParticipantDeletedEvent(quiz.id, participant.id, sequenceNumber = 3))

        await untilAsserted  {
            assertThat(observedQuiz.get())
                    .hasId(quiz.id)
                    .hasNoParticipants()
                    .hasNoQuestions()
                    .undoIsPossible()
                    .isNotFinished
        }
    }

    @Test
    fun shouldHandleParticipantDeletionWhenQuizIsNotInCacheAndLastEventWasAlreadyPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")

        val participant = Participant(name = "Lena")
        val participantDeletedEvent = ParticipantDeletedEvent(quiz.id, participant.id, sequenceNumber = 3)

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, sequenceNumber = 1),
                        ParticipantCreatedEvent(quiz.id, participant, sequenceNumber = 2),
                        participantDeletedEvent
                ))

        val eventBus = EventBus()
        val quizProjection = DefaultQuizProjection(
            eventBus,
            eventRepository,
            UndoneEventsCache(),
            QuizProjectionConfiguration(25, 1)
        )

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(participantDeletedEvent)

        await untilAsserted  {
            assertThat(observedQuiz.get())
                    .hasId(quiz.id)
                    .hasNoParticipants()
                    .hasNoQuestions()
                    .undoIsPossible()
                    .isNotFinished
        }
    }

    @Test
    fun shouldHandleParticipantDeletionWhenQuizIsNotInCacheAndLastEventWasNotYetPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")
        val participant = Participant(name = "Lena")

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, sequenceNumber = 1),
                        ParticipantCreatedEvent(quiz.id, participant, sequenceNumber = 2)
                ))

        val eventBus = EventBus()
        val quizProjection = DefaultQuizProjection(
            eventBus,
            eventRepository,
            UndoneEventsCache(),
            QuizProjectionConfiguration(25, 1)
        )

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(ParticipantDeletedEvent(quiz.id, participant.id, sequenceNumber = 3))

        await untilAsserted  {
            assertThat(observedQuiz.get())
                    .hasId(quiz.id)
                    .hasNoParticipants()
                    .hasNoQuestions()
                    .undoIsPossible()
                    .isNotFinished
        }
    }

}