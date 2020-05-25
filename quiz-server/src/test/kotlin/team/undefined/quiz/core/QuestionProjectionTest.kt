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
        val quiz1Id = UUID.randomUUID()
        val quiz2Id = UUID.randomUUID()

        val eventBus = EventBus()

        val question1 = Question(question = "Warum ist das so?")
        val question2 = Question(question = "Wo ist das?")
        val question3 = Question(question = "Wo ist das?")
        val question4 = Question(question = "Wer ist das?")
        val question5 = Question(question = "Wie ist das?")
        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents()).thenReturn(Flux.just(
                QuizCreatedEvent(quiz1Id, Quiz(name = "Awesome Quiz1")),
                QuestionCreatedEvent(quiz1Id, question1),
                QuestionAskedEvent(quiz1Id, question1.id),
                QuestionCreatedEvent(quiz1Id, question2),
                QuestionAskedEvent(quiz1Id, question2.id),
                QuestionCreatedEvent(quiz1Id, question3),
                QuestionAskedEvent(quiz1Id, question3.id),
                QuestionCreatedEvent(quiz1Id, question4),
                QuestionDeletedEvent(quiz1Id, question4.id),
                QuizCreatedEvent(quiz2Id, Quiz(name = "Awesome Quiz2")),
                QuestionCreatedEvent(quiz2Id, question1),
                QuestionAskedEvent(quiz2Id, question1.id),
                QuestionCreatedEvent(quiz2Id, question2),
                QuestionAskedEvent(quiz2Id, question2.id),
                QuestionCreatedEvent(quiz2Id, question3),
                QuestionAskedEvent(quiz2Id, question3.id),
                QuestionCreatedEvent(quiz2Id, question4),
                QuestionDeletedEvent(quiz2Id, question4.id)
        ))

        val questionProjection = QuestionProjection(eventBus, eventRepository)

        await until {
            questionProjection.determineQuestions().size == 2
                    && questionProjection.determineQuestions()[quiz1Id]!!.isEmpty()
                    && questionProjection.determineQuestions()[quiz2Id]!!.size == 2
                    && questionProjection.determineQuestions()[quiz2Id]!![0].question == "Warum ist das so?"
                    && questionProjection.determineQuestions()[quiz2Id]!![0].pending
                    && questionProjection.determineQuestions()[quiz2Id]!![1].question == "Wo ist das?"
                    && questionProjection.determineQuestions()[quiz2Id]!![1].pending
        }

        eventBus.post(QuestionCreatedEvent(quiz1Id, question5))

        await until {
            questionProjection.determineQuestions().size == 2
                    && questionProjection.determineQuestions()[quiz1Id]!!.isEmpty()
                    && questionProjection.determineQuestions()[quiz2Id]!!.size == 2
                    && questionProjection.determineQuestions()[quiz2Id]!![0].question == "Warum ist das so?"
                    && questionProjection.determineQuestions()[quiz2Id]!![0].pending
                    && questionProjection.determineQuestions()[quiz2Id]!![1].question == "Wo ist das?"
                    && questionProjection.determineQuestions()[quiz2Id]!![1].pending
        }

        eventBus.post(QuestionDeletedEvent(quiz1Id, question5.id))

        await until {
            questionProjection.determineQuestions().size == 2
                    && questionProjection.determineQuestions()[quiz1Id]!!.isEmpty()
                    && questionProjection.determineQuestions()[quiz2Id]!!.size == 2
                    && questionProjection.determineQuestions()[quiz2Id]!![0].question == "Warum ist das so?"
                    && questionProjection.determineQuestions()[quiz2Id]!![0].pending
                    && questionProjection.determineQuestions()[quiz2Id]!![1].question == "Wo ist das?"
                    && questionProjection.determineQuestions()[quiz2Id]!![1].pending
        }
    }

}