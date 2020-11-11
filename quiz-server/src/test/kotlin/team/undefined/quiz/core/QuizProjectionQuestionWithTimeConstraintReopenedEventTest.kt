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
        val quizProjection = QuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), mock(EventRepository::class.java))

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        val question = Question(question = "Wofür steht die Abkürzung a.D.?", initialTimeToAnswer = 5)
        val participant = Participant(name = "Lena")

        eventBus.post(QuizCreatedEvent(quiz.id, quiz, 1))
        eventBus.post(QuestionCreatedEvent(quiz.id, question, 2))
        eventBus.post(ParticipantCreatedEvent(quiz.id, participant, 3))
        eventBus.post(QuestionAskedEvent(quiz.id, question.id, 4))
        eventBus.post(TimeToAnswerDecreasedEvent(quiz.id, question.id, 5))
        eventBus.post(TimeToAnswerDecreasedEvent(quiz.id, question.id, 6))
        eventBus.post(TimeToAnswerDecreasedEvent(quiz.id, question.id, 7))
        eventBus.post(TimeToAnswerDecreasedEvent(quiz.id, question.id, 8))
        eventBus.post(TimeToAnswerDecreasedEvent(quiz.id, question.id, 9))
        eventBus.post(CurrentQuestionReopenedEvent(quiz.id, 10))

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
            assertThat(q.finished).isFalse()
        }
    }

    @Test
    fun shouldHandleQuestionReopenedEventWhenQuizIsNotInCacheAndLastEventWasAlreadyPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")

        val question = Question(question = "Wofür steht die Abkürzung a.D.?", initialTimeToAnswer = 5)
        val reopenedEvent = CurrentQuestionReopenedEvent(quiz.id, 10)

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, 1),
                        QuestionCreatedEvent(quiz.id, question, 2),
                        ParticipantCreatedEvent(quiz.id, Participant(name = "Lena"), 3),
                        QuestionAskedEvent(quiz.id, question.id, 4),
                        TimeToAnswerDecreasedEvent(quiz.id, question.id, 5),
                        TimeToAnswerDecreasedEvent(quiz.id, question.id, 6),
                        TimeToAnswerDecreasedEvent(quiz.id, question.id, 7),
                        TimeToAnswerDecreasedEvent(quiz.id, question.id, 8),
                        TimeToAnswerDecreasedEvent(quiz.id, question.id, 9),
                        reopenedEvent
                ))

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), eventRepository)

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
                        QuizCreatedEvent(quiz.id, quiz, 1),
                        QuestionCreatedEvent(quiz.id, question, 2),
                        ParticipantCreatedEvent(quiz.id, Participant(name = "Lena"), 3),
                        QuestionAskedEvent(quiz.id, question.id, 4),
                        TimeToAnswerDecreasedEvent(quiz.id, question.id, 5),
                        TimeToAnswerDecreasedEvent(quiz.id, question.id, 6),
                        TimeToAnswerDecreasedEvent(quiz.id, question.id, 7),
                        TimeToAnswerDecreasedEvent(quiz.id, question.id, 8),
                        TimeToAnswerDecreasedEvent(quiz.id, question.id, 9)
                ))

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), eventRepository)

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(CurrentQuestionReopenedEvent(quiz.id, 10))

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
            assertThat(q.finished).isFalse()
        }
    }

}