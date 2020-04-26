package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.util.concurrent.atomic.AtomicReference

internal class QuizProjectionQuizCreatedEventTest {

    @Test
    fun shouldHandleQuizCreation() {
        val quiz = Quiz(name = "Awesome Quiz")

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, mock(EventRepository::class.java))

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(QuizCreatedEvent(quiz.id, quiz, 1))

        await until {
            observedQuiz.get() == quiz
        }
    }

}