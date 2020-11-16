package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import team.undefined.quiz.core.QuizAssert.assertThat
import java.util.concurrent.atomic.AtomicReference

internal class QuizProjectionQuizCreatedEventTest {

    @Test
    fun shouldHandleQuizCreation() {
        val quiz = Quiz(name = "Awesome Quiz")

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), mock(EventRepository::class.java), UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(QuizCreatedEvent(quiz.id, quiz, 1))

        await untilAsserted {
            assertThat(observedQuiz.get())
                    .hasId(quiz.id)
                    .undoIsNotPossible()
        }
    }

}