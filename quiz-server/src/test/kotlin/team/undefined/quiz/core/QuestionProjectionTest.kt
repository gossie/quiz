package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import reactor.core.publisher.Flux
import java.util.*

internal class QuestionProjectionTest {

    @Test
    fun shouldInitializeProjectionAndHandleEvent() {
        val quizId = UUID.randomUUID()

        val eventBus = EventBus()

        val question1 = Question(question = "Warum ist das so?")
        val question2 = Question(question = "Wo ist das?")
        val question3 = Question(question = "Wer ist das?")
        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents()).thenReturn(Flux.just(
                QuizCreatedEvent(quizId, Quiz(name = "Awesome Quiz")),
                QuestionCreatedEvent(quizId, question1),
                QuestionCreatedEvent(quizId, question2)
        ))

        val questionProjection = QuestionProjection(eventBus, eventRepository)

        await until {
            questionProjection.determineQuestions().size == 2
                    &&  questionProjection.determineQuestions().contains(question1)
                    &&  questionProjection.determineQuestions().contains(question2)
        }

        eventBus.post(QuestionCreatedEvent(quizId, question3))

        await until {
            questionProjection.determineQuestions().size == 3
                    &&  questionProjection.determineQuestions().contains(question1)
                    &&  questionProjection.determineQuestions().contains(question2)
                    &&  questionProjection.determineQuestions().contains(question3)
        }
    }

}