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

internal class QuizProjectionQuestionReopenedEventTest {

    @Test
    fun shouldHandleQuestionReopenedEventWhenQuizIsAlreadyInCache() {
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
        eventBus.post(AnsweredEvent(quiz.id, participant.id, AnswerCommand.Answer.INCORRECT, 6))
        eventBus.post(CurrentQuestionReopenedEvent(quiz.id, 7))

        await untilAsserted {
            val q = observedQuiz.get()

            assertThat(q.id).isEqualTo(quiz.id)
            assertThat(q.participants).hasSize(1)
            assertThat(q.participants[0].turn).isFalse()
            assertThat(q.participants[0].points).isEqualTo(0L)
            assertThat(q.questions).hasSize(1)
            assertThat(q.questions[0].pending).isTrue()
            assertThat(q.questions[0].alreadyPlayed).isFalse()
            assertThat(q.undoPossible).isTrue()
            assertThat(q.finished).isFalse()
        }
    }

    @Test
    fun shouldHandleQuestionReopenedEventWhenQuizIsNotInCacheAndLastEventWasAlreadyPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")

        val question = Question(question = "Wofür steht die Abkürzung a.D.?")
        val participant = Participant(name = "Lena")
        val reopenedEvent = CurrentQuestionReopenedEvent(quiz.id, 7)

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, 1),
                        QuestionCreatedEvent(quiz.id, question, 2),
                        ParticipantCreatedEvent(quiz.id, Participant(name = "Lena"), 3),
                        QuestionAskedEvent(quiz.id, question.id, 4),
                        BuzzeredEvent(quiz.id, participant.id, 5),
                        AnsweredEvent(quiz.id, participant.id, AnswerCommand.Answer.INCORRECT, 6),
                        reopenedEvent
                ))

        val eventBus = EventBus()
        val quizProjection = DefaultQuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), eventRepository, UndoneEventsCache())

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
            assertThat(q.undoPossible).isTrue()
            assertThat(q.finished).isFalse()
        }
    }

    @Test
    fun shouldHandleQuestionReopenedEventCreationWhenQuizIsNotInCacheAndLastEventWasNotYetPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")
        val question = Question(question = "Wofür steht die Abkürzung a.D.?")
        val participant = Participant(name = "Lena")

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, 1),
                        QuestionCreatedEvent(quiz.id, question, 2),
                        ParticipantCreatedEvent(quiz.id, participant, 3),
                        QuestionAskedEvent(quiz.id, question.id, 4),
                        BuzzeredEvent(quiz.id, participant.id, 5),
                        AnsweredEvent(quiz.id, participant.id, AnswerCommand.Answer.INCORRECT, 6)
                ))

        val eventBus = EventBus()
        val quizProjection = DefaultQuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), eventRepository, UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(CurrentQuestionReopenedEvent(quiz.id, 7))

        await untilAsserted {
            val q = observedQuiz.get()

            assertThat(q.id).isEqualTo(quiz.id)
            assertThat(q.participants).hasSize(1)
            assertThat(q.participants[0].turn).isFalse()
            assertThat(q.participants[0].points).isEqualTo(0L)
            assertThat(q.questions).hasSize(1)
            assertThat(q.questions[0].pending).isTrue()
            assertThat(q.questions[0].alreadyPlayed).isFalse()
            assertThat(q.undoPossible).isTrue()
            assertThat(q.finished).isFalse()
        }
    }

}