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

internal class QuizProjectionQuestionWithTimeConstraintReopenedEventTest {

    @Test
    fun shouldHandleQuestionReopenedEventWhenQuizIsAlreadyInCache() {
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

        val question = Question(question = "Wofür steht die Abkürzung a.D.?", initialTimeToAnswer = 5)
        val participant = Participant(name = "Lena")

        eventBus.post(QuizCreatedEvent(quiz.id, quiz, sequenceNumber = 1))
        eventBus.post(QuestionCreatedEvent(quiz.id, question, sequenceNumber = 2))
        eventBus.post(ParticipantCreatedEvent(quiz.id, participant, sequenceNumber = 3))
        eventBus.post(QuestionAskedEvent(quiz.id, question.id, sequenceNumber = 4))
        eventBus.post(TimeToAnswerDecreasedEvent(quiz.id, question.id, sequenceNumber = 5))
        eventBus.post(TimeToAnswerDecreasedEvent(quiz.id, question.id, sequenceNumber = 6))
        eventBus.post(TimeToAnswerDecreasedEvent(quiz.id, question.id, sequenceNumber = 7))
        eventBus.post(TimeToAnswerDecreasedEvent(quiz.id, question.id, sequenceNumber = 8))
        eventBus.post(TimeToAnswerDecreasedEvent(quiz.id, question.id, sequenceNumber = 9))
        eventBus.post(CurrentQuestionReopenedEvent(quiz.id, sequenceNumber = 10))

        await untilAsserted {
            val q = observedQuiz.get()

            assertThat(q.id).isEqualTo(quiz.id)
            assertThat(q.participants).hasSize(1)
            assertThat(q.participants[0].turn).isFalse()
            assertThat(q.participants[0].points).isEqualTo(0L)
            assertThat(q.questions).hasSize(1)
            assertThat(q.questions[0].pending).isTrue()
            assertThat(q.questions[0].alreadyPlayed).isFalse()
            assertThat(q.questions[0].initialTimeToAnswer).isEqualTo(5)
            assertThat(q.questions[0].secondsLeft).isEqualTo(5)
            assertThat(q.undoPossible).isTrue()
            assertThat(q.finished).isFalse()
        }
    }

    @Test
    fun shouldHandleQuestionReopenedEventWhenQuizIsNotInCacheAndLastEventWasAlreadyPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")

        val question = Question(question = "Wofür steht die Abkürzung a.D.?", initialTimeToAnswer = 5)
        val reopenedEvent = CurrentQuestionReopenedEvent(quiz.id, sequenceNumber = 10)

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, sequenceNumber = 1),
                        QuestionCreatedEvent(quiz.id, question, sequenceNumber = 2),
                        ParticipantCreatedEvent(quiz.id, Participant(name = "Lena"), sequenceNumber = 3),
                        QuestionAskedEvent(quiz.id, question.id, sequenceNumber = 4),
                        TimeToAnswerDecreasedEvent(quiz.id, question.id, sequenceNumber = 5),
                        TimeToAnswerDecreasedEvent(quiz.id, question.id, sequenceNumber = 6),
                        TimeToAnswerDecreasedEvent(quiz.id, question.id, sequenceNumber = 7),
                        TimeToAnswerDecreasedEvent(quiz.id, question.id, sequenceNumber = 8),
                        TimeToAnswerDecreasedEvent(quiz.id, question.id, sequenceNumber = 9),
                        reopenedEvent
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

        eventBus.post(reopenedEvent)

        await untilAsserted {
            val q = observedQuiz.get()

            assertThat(q.id).isEqualTo(quiz.id)
            assertThat(q.participants).hasSize(1)
            assertThat(q.participants[0].turn).isFalse()
            assertThat(q.participants[0].points).isEqualTo(0L)
            assertThat(q.questions).hasSize(1)
            assertThat(q.questions[0].pending).isTrue()
            assertThat(q.questions[0].alreadyPlayed).isFalse()
            assertThat(q.questions[0].initialTimeToAnswer).isEqualTo(5)
            assertThat(q.questions[0].secondsLeft).isEqualTo(5)
            assertThat(q.undoPossible).isTrue()
            assertThat(q.finished).isFalse()
        }
    }

    @Test
    fun shouldHandleQuestionReopenedEventCreationWhenQuizIsNotInCacheAndLastEventWasNotYetPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")
        val question = Question(question = "Wofür steht die Abkürzung a.D.?", initialTimeToAnswer = 5)

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, sequenceNumber = 1),
                        QuestionCreatedEvent(quiz.id, question, sequenceNumber = 2),
                        ParticipantCreatedEvent(quiz.id, Participant(name = "Lena"), sequenceNumber = 3),
                        QuestionAskedEvent(quiz.id, question.id, sequenceNumber = 4),
                        TimeToAnswerDecreasedEvent(quiz.id, question.id, sequenceNumber = 5),
                        TimeToAnswerDecreasedEvent(quiz.id, question.id, sequenceNumber = 6),
                        TimeToAnswerDecreasedEvent(quiz.id, question.id, sequenceNumber = 7),
                        TimeToAnswerDecreasedEvent(quiz.id, question.id, sequenceNumber = 8),
                        TimeToAnswerDecreasedEvent(quiz.id, question.id, sequenceNumber = 9)
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

        eventBus.post(CurrentQuestionReopenedEvent(quiz.id, sequenceNumber = 10))

        await untilAsserted {
            val q = observedQuiz.get()

            assertThat(q.id).isEqualTo(quiz.id)
            assertThat(q.participants).hasSize(1)
            assertThat(q.participants[0].turn).isFalse
            assertThat(q.participants[0].points).isEqualTo(0L)
            assertThat(q.questions).hasSize(1)
            assertThat(q.questions[0].pending).isTrue
            assertThat(q.questions[0].alreadyPlayed).isFalse
            assertThat(q.questions[0].initialTimeToAnswer).isEqualTo(5)
            assertThat(q.questions[0].secondsLeft).isEqualTo(5)
            assertThat(q.undoPossible).isTrue
            assertThat(q.finished).isFalse
        }
    }

}