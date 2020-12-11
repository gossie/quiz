package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import reactor.core.publisher.Flux
import java.util.concurrent.atomic.AtomicReference

internal class QuizProjectionEstimationQuestionAnsweredCorrectEventTest {

    @Test
    fun shouldHandleQuestionAnsweredCorrectEventWhenQuizIsAlreadyInCache() {
        val quiz = Quiz(name = "Awesome Quiz")

        val eventBus = EventBus()
        val quizProjection = DefaultQuizProjection(eventBus, mock(EventRepository::class.java), UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        val question = Question(question = "Wie hoch ist das?", estimates = HashMap())
        val participant1 = Participant(name = "Lena")
        val participant2 = Participant(name = "André")

        eventBus.post(QuizCreatedEvent(quiz.id, quiz, sequenceNumber = 1))
        eventBus.post(QuestionCreatedEvent(quiz.id, question, sequenceNumber = 2))
        eventBus.post(ParticipantCreatedEvent(quiz.id, participant1, sequenceNumber = 3))
        eventBus.post(ParticipantCreatedEvent(quiz.id, participant2, sequenceNumber = 4))
        eventBus.post(QuestionAskedEvent(quiz.id, question.id, sequenceNumber = 5))
        eventBus.post(EstimatedEvent(quiz.id, participant1.id, "1000", sequenceNumber = 6))
        eventBus.post(EstimatedEvent(quiz.id, participant2.id, "1500", sequenceNumber = 7))
        eventBus.post(AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, sequenceNumber = 8))

        await until {
            observedQuiz.get().id == quiz.id
                    && observedQuiz.get().participants.size == 2
                    && observedQuiz.get().participants[0].points == 2L
                    && observedQuiz.get().participants[1].points == 0L
                    && observedQuiz.get().questions.size == 1
                    && observedQuiz.get().questions[0].pending
                    && !observedQuiz.get().questions[0].alreadyPlayed
                    && observedQuiz.get().undoPossible
                    && !observedQuiz.get().finished
        }
    }

    @Test
    fun shouldHandleQuestionAnsweredCorrectEventWhenQuizIsNotInCacheAndLastEventWasAlreadyPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")

        val question = Question(question = "Wie hoch ist das?", estimates = HashMap())
        val participant1 = Participant(name = "Lena")
        val participant2 = Participant(name = "André")
        val answeredEvent = AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, sequenceNumber = 8)

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, sequenceNumber = 1),
                        QuestionCreatedEvent(quiz.id, question, sequenceNumber = 2),
                        ParticipantCreatedEvent(quiz.id, participant1, sequenceNumber = 3),
                        ParticipantCreatedEvent(quiz.id, participant2, sequenceNumber = 4),
                        QuestionAskedEvent(quiz.id, question.id, sequenceNumber = 5),
                        EstimatedEvent(quiz.id, participant1.id, "1000", sequenceNumber = 6),
                        EstimatedEvent(quiz.id, participant2.id, "1000", sequenceNumber = 7),
                        answeredEvent
                ))

        val eventBus = EventBus()
        val quizProjection = DefaultQuizProjection(eventBus, eventRepository, UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(answeredEvent)

        await until {
            observedQuiz.get().id == quiz.id
                    && observedQuiz.get().participants.size == 2
                    && observedQuiz.get().participants[0].points == 2L
                    && observedQuiz.get().participants[1].points == 0L
                    && observedQuiz.get().questions.size == 1
                    && observedQuiz.get().questions[0].pending
                    && !observedQuiz.get().questions[0].alreadyPlayed
                    && observedQuiz.get().undoPossible
                    && !observedQuiz.get().finished
        }
    }

    @Test
    fun shouldHandleQuestionAnsweredCorrectEventCreationWhenQuizIsNotInCacheAndLastEventWasNotYetPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")
        val question = Question(question = "Wie hoch ist das?", estimates = HashMap())
        val participant1 = Participant(name = "Lena")
        val participant2 = Participant(name = "André")

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, sequenceNumber = 1),
                        QuestionCreatedEvent(quiz.id, question, sequenceNumber = 2),
                        ParticipantCreatedEvent(quiz.id, participant1, sequenceNumber = 3),
                        ParticipantCreatedEvent(quiz.id, participant2, sequenceNumber = 4),
                        QuestionAskedEvent(quiz.id, question.id, sequenceNumber = 5),
                        EstimatedEvent(quiz.id, participant1.id, "1000", sequenceNumber = 6),
                        EstimatedEvent(quiz.id, participant2.id, "1000", sequenceNumber = 7)
                ))

        val eventBus = EventBus()
        val quizProjection = DefaultQuizProjection(eventBus, eventRepository, UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, sequenceNumber = 8))

        await until {
            observedQuiz.get().id == quiz.id
                    && observedQuiz.get().participants.size == 2
                    && observedQuiz.get().participants[0].points == 2L
                    && observedQuiz.get().participants[1].points == 0L
                    && observedQuiz.get().questions.size == 1
                    && observedQuiz.get().questions[0].pending
                    && !observedQuiz.get().questions[0].alreadyPlayed
                    && observedQuiz.get().undoPossible
                    && !observedQuiz.get().finished
        }
    }

}