package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import reactor.core.publisher.Flux
import java.util.concurrent.atomic.AtomicReference

internal class QuizProjectionQuestionAskedEventTest {

    @Test
    fun shouldHandleQuestionAskedEventWhenQuizIsAlreadyInCache_askQuestion() {
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
        eventBus.post(QuestionAskedEvent(quiz.id, question.id, 4))

        await until {
            observedQuiz.get().id == quiz.id
                    && observedQuiz.get().participants.size == 1
                    && observedQuiz.get().questions.size == 1
                    && observedQuiz.get().questions[0].pending
                    && observedQuiz.get().isUndoPossible()
                    && !observedQuiz.get().finished
        }
    }

    @Test
    fun shouldHandleQuestionAskedEventWhenQuizIsNotInCacheAndLastEventWasAlreadyPersisted_askQuestion() {
        val quiz = Quiz(name = "Awesome Quiz")

        val question = Question(question = "Wofür steht die Abkürzung a.D.?")
        val questionAskedEvent = QuestionAskedEvent(quiz.id, question.id, 4)

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, 1),
                        QuestionCreatedEvent(quiz.id, question, 2),
                        ParticipantCreatedEvent(quiz.id, Participant(name = "Lena"), 3),
                        questionAskedEvent
                ))

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), eventRepository, UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(questionAskedEvent)

        await until {
            observedQuiz.get().id == quiz.id
                    && observedQuiz.get().participants.size == 1
                    && observedQuiz.get().questions.size == 1
                    && observedQuiz.get().questions[0].pending
                    && observedQuiz.get().isUndoPossible()
                    && !observedQuiz.get().finished
        }
    }

    @Test
    fun shouldHandleQuestionAskedEventCreationWhenQuizIsNotInCacheAndLastEventWasNotYetPersisted_askQuestion() {
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

        eventBus.post(QuestionAskedEvent(quiz.id, question.id, 4))

        await until {
            observedQuiz.get().id == quiz.id
                    && observedQuiz.get().participants.size == 1
                    && observedQuiz.get().questions.size == 1
                    && observedQuiz.get().questions[0].pending
                    && observedQuiz.get().isUndoPossible()
                    && !observedQuiz.get().finished
        }
    }

    @Test
    fun shouldHandleQuestionAskedEventWhenQuizIsAlreadyInCache_revertQuestion() {
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
        eventBus.post(QuestionAskedEvent(quiz.id, question.id, 4))
        eventBus.post(QuestionAskedEvent(quiz.id, question.id, 5))

        await until {
            observedQuiz.get().id == quiz.id
                    && observedQuiz.get().participants.size == 1
                    && observedQuiz.get().questions.size == 1
                    && !observedQuiz.get().questions[0].pending
                    && observedQuiz.get().isUndoPossible()
                    && !observedQuiz.get().finished
        }
    }

    @Test
    fun shouldHandleQuestionAskedEventWhenQuizIsNotInCacheAndLastEventWasAlreadyPersisted_revertQuestion() {
        val quiz = Quiz(name = "Awesome Quiz")

        val question = Question(question = "Wofür steht die Abkürzung a.D.?")
        val questionAskedEvent = QuestionAskedEvent(quiz.id, question.id, 5)

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, 1),
                        QuestionCreatedEvent(quiz.id, question, 2),
                        ParticipantCreatedEvent(quiz.id, Participant(name = "Lena"), 3),
                        QuestionAskedEvent(quiz.id, question.id, 4),
                        questionAskedEvent
                ))

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), eventRepository, UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(questionAskedEvent)

        await until {
            observedQuiz.get().id == quiz.id
                    && observedQuiz.get().participants.size == 1
                    && observedQuiz.get().questions.size == 1
                    && !observedQuiz.get().questions[0].pending
                    && observedQuiz.get().isUndoPossible()
                    && !observedQuiz.get().finished
        }
    }

    @Test
    fun shouldHandleQuestionAskedEventCreationWhenQuizIsNotInCacheAndLastEventWasNotYetPersisted_revertQuestion() {
        val quiz = Quiz(name = "Awesome Quiz")
        val question = Question(question = "Wofür steht die Abkürzung a.D.?")

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, 1),
                        QuestionCreatedEvent(quiz.id, question, 2),
                        ParticipantCreatedEvent(quiz.id, Participant(name = "Lena"), 3),
                        QuestionAskedEvent(quiz.id, question.id, 4)
                ))

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), eventRepository, UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(QuestionAskedEvent(quiz.id, question.id, 5))

        await until {
            observedQuiz.get().id == quiz.id
                    && observedQuiz.get().participants.size == 1
                    && observedQuiz.get().questions.size == 1
                    && !observedQuiz.get().questions[0].pending
                    && observedQuiz.get().isUndoPossible()
                    && !observedQuiz.get().finished
        }
    }

    @Test
    fun shouldHandleMultipleQuestionAskedEventsWithoutAnswerInBetween() {
        val quiz = Quiz(name = "Awesome Quiz")

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), mock(EventRepository::class.java), UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        val question1 = Question(question = "Wofür steht die Abkürzung a.D.?")
        val question2 = Question(question = "Was ist die Wurzel aus 1/4?")
        eventBus.post(QuizCreatedEvent(quiz.id, quiz, 1))
        eventBus.post(QuestionCreatedEvent(quiz.id, question1, 2))
        eventBus.post(QuestionCreatedEvent(quiz.id, question2, 3))
        eventBus.post(ParticipantCreatedEvent(quiz.id, Participant(name = "Lena"), 4))
        eventBus.post(QuestionAskedEvent(quiz.id, question1.id, 5))
        eventBus.post(QuestionAskedEvent(quiz.id, question2.id, 6))

        await untilAsserted {
            val q = observedQuiz.get()
            assertThat(q.id).isEqualTo(quiz.id)
            assertThat(q.participants).hasSize(1)
            assertThat(q.questions).hasSize(2)
            assertThat(q.questions[0].pending).isFalse()
            assertThat(q.questions[0].alreadyPlayed).isTrue()
            assertThat(q.questions[1].pending).isTrue()
            assertThat(q.questions[1].alreadyPlayed).isFalse()
            assertThat(q.isUndoPossible()).isTrue()
            assertThat(q.finished).isFalse()
        }
    }

}