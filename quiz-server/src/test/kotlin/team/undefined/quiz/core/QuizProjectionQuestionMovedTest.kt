package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import reactor.core.publisher.Flux
import java.util.*
import java.util.concurrent.atomic.AtomicReference

internal class QuizProjectionQuestionMovedTest {

    @Test
    fun shouldHandleQuestionMove() {
        val quiz = Quiz(name = "Awesome Quiz")

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), mock(EventRepository::class.java), UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        val question1 = Question(question = "Wer ist das?")
        val question2 = Question(question = "Was ist das?")
        val question3 = Question(question = "Wo ist das?")

        eventBus.post(QuizCreatedEvent(quiz.id, quiz, 1))
        eventBus.post(QuestionCreatedEvent(quiz.id, question1, 2))
        eventBus.post(QuestionCreatedEvent(quiz.id, question2, 3))
        eventBus.post(QuestionCreatedEvent(quiz.id, question3, 4))

        await untilAsserted {
            QuizAssert.assertThat(observedQuiz.get())
                    .hasId(quiz.id)
                    .hasNoParticipants()
                    .questionSizeIs(3)
                    .hasQuestion(0) { it.hasId(question1.id).hasPreviousQuestionId(null) }
                    .hasQuestion(1) { it.hasId(question2.id).hasPreviousQuestionId(question1.id) }
                    .hasQuestion(2) { it.hasId(question3.id).hasPreviousQuestionId(question2.id) }
        }

        eventBus.post(QuestionEditedEvent(quiz.id, Question(id=question3.id, question = "Wo ist das?", previousQuestionId = question1.id), 5))

        await untilAsserted {
            QuizAssert.assertThat(observedQuiz.get())
                    .hasId(quiz.id)
                    .hasNoParticipants()
                    .questionSizeIs(3)
                    .hasQuestion(0) { it.hasId(question1.id).hasPreviousQuestionId(null) }
                    .hasQuestion(1) { it.hasId(question3.id).hasPreviousQuestionId(question1.id) }
                    .hasQuestion(2) { it.hasId(question2.id).hasPreviousQuestionId(question3.id) }
        }
    }

    @Test
    fun shouldHandleQuestionMoveToTop() {
        val quiz = Quiz(name = "Awesome Quiz")

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), mock(EventRepository::class.java), UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        val question1 = Question(question = "Wer ist das?")
        val question2 = Question(question = "Was ist das?")
        val question3 = Question(question = "Wo ist das?")

        eventBus.post(QuizCreatedEvent(quiz.id, quiz, 1))
        eventBus.post(QuestionCreatedEvent(quiz.id, question1, 2))
        eventBus.post(QuestionCreatedEvent(quiz.id, question2, 3))
        eventBus.post(QuestionCreatedEvent(quiz.id, question3, 4))

        await untilAsserted {
            QuizAssert.assertThat(observedQuiz.get())
                    .hasId(quiz.id)
                    .hasNoParticipants()
                    .questionSizeIs(3)
                    .hasQuestion(0) { it.hasId(question1.id).hasPreviousQuestionId(null) }
                    .hasQuestion(1) { it.hasId(question2.id).hasPreviousQuestionId(question1.id) }
                    .hasQuestion(2) { it.hasId(question3.id).hasPreviousQuestionId(question2.id) }
        }

        eventBus.post(QuestionEditedEvent(quiz.id, Question(id=question3.id, question = "Wo ist das?"), 5))

        await untilAsserted {
            QuizAssert.assertThat(observedQuiz.get())
                    .hasId(quiz.id)
                    .hasNoParticipants()
                    .questionSizeIs(3)
                    .hasQuestion(0) { it.hasId(question3.id).hasPreviousQuestionId(null) }
                    .hasQuestion(1) { it.hasId(question1.id).hasPreviousQuestionId(question3.id) }
                    .hasQuestion(2) { it.hasId(question2.id).hasPreviousQuestionId(question1.id) }
        }
    }

    @Test
    fun shouldHandleQuestionMoveToBottom() {
        val quiz = Quiz(name = "Awesome Quiz")

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), mock(EventRepository::class.java), UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        val question1 = Question(question = "Wer ist das?")
        val question2 = Question(question = "Was ist das?")
        val question3 = Question(question = "Wo ist das?")

        eventBus.post(QuizCreatedEvent(quiz.id, quiz, 1))
        eventBus.post(QuestionCreatedEvent(quiz.id, question1, 2))
        eventBus.post(QuestionCreatedEvent(quiz.id, question2, 3))
        eventBus.post(QuestionCreatedEvent(quiz.id, question3, 4))

        await untilAsserted {
            QuizAssert.assertThat(observedQuiz.get())
                    .hasId(quiz.id)
                    .hasNoParticipants()
                    .questionSizeIs(3)
                    .hasQuestion(0) { it.hasId(question1.id).hasPreviousQuestionId(null) }
                    .hasQuestion(1) { it.hasId(question2.id).hasPreviousQuestionId(question1.id) }
                    .hasQuestion(2) { it.hasId(question3.id).hasPreviousQuestionId(question2.id) }
        }

        eventBus.post(QuestionEditedEvent(quiz.id, Question(id=question1.id, question = "Wer ist das?", previousQuestionId = question3.id), 5))

        await untilAsserted {
            QuizAssert.assertThat(observedQuiz.get())
                    .hasId(quiz.id)
                    .hasNoParticipants()
                    .questionSizeIs(3)
                    .hasQuestion(0) { it.hasId(question2.id).hasPreviousQuestionId(null) }
                    .hasQuestion(1) { it.hasId(question3.id).hasPreviousQuestionId(question2.id) }
                    .hasQuestion(2) { it.hasId(question1.id).hasPreviousQuestionId(question3.id) }
        }
    }

}