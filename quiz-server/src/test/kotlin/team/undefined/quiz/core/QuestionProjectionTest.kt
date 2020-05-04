package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
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
        val question3 = Question(question = "Wo ist das?")
        val question4 = Question(question = "Wer ist das?")
        val question5 = Question(question = "Wie ist das?")
        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents()).thenReturn(Flux.just(
                QuizCreatedEvent(quizId, Quiz(name = "Awesome Quiz")),
                QuestionCreatedEvent(quizId, question1),
                QuestionCreatedEvent(quizId, question2),
                QuestionCreatedEvent(quizId, question3),
                QuestionCreatedEvent(quizId, question4),
                QuestionDeletedEvent(quizId, question4.id)
        ))

        val questionProjection = QuestionProjection(eventBus, eventRepository)

        await until {
            questionProjection.determineQuestions().size == 2
                    &&  questionProjection.determineQuestions().contains("Warum ist das so?")
                    &&  questionProjection.determineQuestions().contains("Wo ist das?")
        }

        eventBus.post(QuestionCreatedEvent(quizId, question5))

        await until {
            questionProjection.determineQuestions().size == 3
                    &&  questionProjection.determineQuestions().contains("Warum ist das so?")
                    &&  questionProjection.determineQuestions().contains("Wo ist das?")
                    &&  questionProjection.determineQuestions().contains("Wie ist das?")
        }

        eventBus.post(QuestionDeletedEvent(quizId, question5.id))

        await until {
            questionProjection.determineQuestions().size == 2
                    &&  questionProjection.determineQuestions().contains("Warum ist das so?")
                    &&  questionProjection.determineQuestions().contains("Wo ist das?")
        }
    }

}