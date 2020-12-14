package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import reactor.core.publisher.Flux
import team.undefined.quiz.core.QuizAssert.assertThat
import java.util.concurrent.atomic.AtomicReference

internal class QuizProjectionForceEmitCommandTest {

    @Test
    fun shouldForceEmitOfQuiz() {
        val quiz = Quiz(name = "Awesome Quiz", sequenceNumber = 0, timestamp = 1)

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(QuizCreatedEvent(quiz.id, quiz, 1, 0)))

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

        eventBus.post(ForceEmitCommand(quiz.id))

        await untilAsserted  {
            assertThat(observedQuiz.get()).isEqualTo(quiz)
        }
    }

}
