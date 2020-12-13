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

internal class QuizProjectionQuestionAnsweredCorrectEventTest {

    @Test
    fun shouldHandleQuestionAnsweredCorrectEventWhenQuizIsAlreadyInCache() {
        val quiz = Quiz(name = "Awesome Quiz")

        val eventBus = EventBus()
        val quizProjection = DefaultQuizProjection(
            eventBus,
            mock(EventRepository::class.java),
            UndoneEventsCache(),
            QuizProjectionConfiguration(25)
        )

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        val question = Question(question = "Wofür steht die Abkürzung a.D.?")
        val participant = Participant(name = "Lena")

        eventBus.post(QuizCreatedEvent(quiz.id, quiz, sequenceNumber = 1))
        eventBus.post(QuestionCreatedEvent(quiz.id, question, sequenceNumber = 2))
        eventBus.post(ParticipantCreatedEvent(quiz.id, participant, sequenceNumber = 3))
        eventBus.post(QuestionAskedEvent(quiz.id, question.id, sequenceNumber = 4))
        eventBus.post(BuzzeredEvent(quiz.id, participant.id, sequenceNumber = 5))
        eventBus.post(AnsweredEvent(quiz.id, participant.id, AnswerCommand.Answer.CORRECT, sequenceNumber = 6))

        await untilAsserted  {
            val q = observedQuiz.get()

            assertThat(q.id).isEqualTo(quiz.id)
            assertThat(q.participants).hasSize(1)
            assertThat(q.participants[0].points).isEqualTo(2L)
            assertThat(q.questions).hasSize(1)
            assertThat(q.questions[0].pending).isTrue()
            assertThat(q.questions[0].revealed).isFalse()
            assertThat(q.questions[0].alreadyPlayed).isFalse()
            assertThat(q.finished).isFalse()
        }
    }

    @Test
    fun shouldHandleOldQuestionAnsweredCorrectEventWhenQuizIsAlreadyInCache() {
        val quiz = Quiz(name = "Awesome Quiz")

        val eventBus = EventBus()
        val quizProjection = DefaultQuizProjection(
            eventBus,
            mock(EventRepository::class.java),
            UndoneEventsCache(),
            QuizProjectionConfiguration(25)
        )

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        val question = Question(question = "Wofür steht die Abkürzung a.D.?")
        val participant = Participant(name = "Lena")

        eventBus.post(QuizCreatedEvent(quiz.id, quiz, sequenceNumber = 1))
        eventBus.post(QuestionCreatedEvent(quiz.id, question, sequenceNumber = 2))
        eventBus.post(ParticipantCreatedEvent(quiz.id, participant, sequenceNumber = 3))
        eventBus.post(QuestionAskedEvent(quiz.id, question.id, sequenceNumber = 4))
        eventBus.post(BuzzeredEvent(quiz.id, participant.id, sequenceNumber = 5))
        eventBus.post(AnsweredEvent(quiz.id, participant.id, AnswerCommand.Answer.CORRECT, sequenceNumber = 6))

        await untilAsserted  {
            val q = observedQuiz.get()

            assertThat(q.id).isEqualTo(quiz.id)
            assertThat(q.participants).hasSize(1)
            assertThat(q.participants[0].points).isEqualTo(2L)
            assertThat(q.questions).hasSize(1)
            assertThat(q.questions[0].pending).isTrue()
            assertThat(q.questions[0].revealed).isFalse()
            assertThat(q.questions[0].alreadyPlayed).isFalse()
            assertThat(q.undoPossible).isTrue()
            assertThat(q.finished).isFalse()
        }
    }

    @Test
    fun shouldHandleQuestionAnsweredCorrectEventWhenQuizIsNotInCacheAndLastEventWasAlreadyPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")

        val question = Question(question = "Wofür steht die Abkürzung a.D.?")
        val participant = Participant(name = "Lena")
        val answeredEvent = AnsweredEvent(quiz.id, participant.id, AnswerCommand.Answer.CORRECT, sequenceNumber = 6)

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, sequenceNumber = 1),
                        QuestionCreatedEvent(quiz.id, question, sequenceNumber = 2),
                        ParticipantCreatedEvent(quiz.id, participant, sequenceNumber = 3),
                        QuestionAskedEvent(quiz.id, question.id, sequenceNumber = 4),
                        BuzzeredEvent(quiz.id, participant.id, sequenceNumber = 5),
                        answeredEvent
                ))

        val eventBus = EventBus()
        val quizProjection = DefaultQuizProjection(
            eventBus,
            eventRepository,
            UndoneEventsCache(),
            QuizProjectionConfiguration(25)
        )

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(answeredEvent)

        await untilAsserted  {
            val q = observedQuiz.get()

            assertThat(q.id).isEqualTo(quiz.id)
            assertThat(q.participants).hasSize(1)
            assertThat(q.participants[0].points).isEqualTo(2L)
            assertThat(q.questions).hasSize(1)
            assertThat(q.questions[0].pending).isTrue()
            assertThat(q.questions[0].revealed).isFalse()
            assertThat(q.questions[0].alreadyPlayed).isFalse()
            assertThat(q.undoPossible).isTrue()
            assertThat(q.finished).isFalse()
        }
    }

    @Test
    fun shouldHandleQuestionAnsweredCorrectEventCreationWhenQuizIsNotInCacheAndLastEventWasNotYetPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")
        val question = Question(question = "Wofür steht die Abkürzung a.D.?")
        val participant = Participant(name = "Lena")

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, sequenceNumber = 1),
                        QuestionCreatedEvent(quiz.id, question, sequenceNumber = 2),
                        ParticipantCreatedEvent(quiz.id, participant, sequenceNumber = 3),
                        QuestionAskedEvent(quiz.id, question.id, sequenceNumber = 4),
                        BuzzeredEvent(quiz.id, participant.id, sequenceNumber = 5)
                ))

        val eventBus = EventBus()
        val quizProjection = DefaultQuizProjection(
            eventBus,
            eventRepository,
            UndoneEventsCache(),
            QuizProjectionConfiguration(25)
        )

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(AnsweredEvent(quiz.id, participant.id, AnswerCommand.Answer.CORRECT, sequenceNumber = 6))

        await untilAsserted  {
            val q = observedQuiz.get()

            assertThat(q.id).isEqualTo(quiz.id)
            assertThat(q.participants).hasSize(1)
            assertThat(q.participants[0].points).isEqualTo(2L)
            assertThat(q.questions).hasSize(1)
            assertThat(q.questions[0].pending).isTrue()
            assertThat(q.questions[0].revealed).isFalse()
            assertThat(q.questions[0].alreadyPlayed).isFalse()
            assertThat(q.undoPossible).isTrue()
            assertThat(q.finished).isFalse()
        }
    }

}