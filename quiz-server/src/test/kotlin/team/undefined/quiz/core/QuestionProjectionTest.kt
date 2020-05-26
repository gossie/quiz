package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
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

        val question1 = Question(question = "Warum ist das so?", visibility = Question.QuestionVisibility.PUBLIC)
        val question2 = Question(question = "Wo ist das?", visibility = Question.QuestionVisibility.PUBLIC)
        val question3 = Question(question = "Wo ist das?", visibility = Question.QuestionVisibility.PUBLIC)
        val privateQuestion = Question(question = "Frage mit privatem Inhalt?")
        val question4 = Question(question = "Wer ist das?", visibility = Question.QuestionVisibility.PUBLIC)
        val question5 = Question(question = "Wie ist das?", visibility = Question.QuestionVisibility.PUBLIC)
        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents()).thenReturn(Flux.just(
                QuizCreatedEvent(quiz1Id, Quiz(name = "Awesome Quiz1")),
                QuestionCreatedEvent(quiz1Id, question1),
                QuestionAskedEvent(quiz1Id, question1.id),
                QuestionCreatedEvent(quiz1Id, question2),
                QuestionAskedEvent(quiz1Id, question2.id),
                QuestionCreatedEvent(quiz1Id, question3),
                QuestionAskedEvent(quiz1Id, question3.id),
                QuestionCreatedEvent(quiz1Id, privateQuestion),
                QuestionAskedEvent(quiz1Id, privateQuestion.id),
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

        await untilAsserted {
            val questions = questionProjection.determineQuestions()

            assertThat(questions).hasSize(2)

            var emptyQuiz: UUID
            var filledQuiz: UUID

            if (questions[quiz1Id]!!.isEmpty()) {
                emptyQuiz = quiz1Id
                filledQuiz = quiz2Id
            } else {
                emptyQuiz = quiz2Id
                filledQuiz = quiz1Id
            }

            assertThat(questions[emptyQuiz]).isEmpty()
            assertThat(questions[filledQuiz]).hasSize(2)
            assertThat(questions[filledQuiz]!![0].question).isEqualTo("Warum ist das so?")
            assertThat(questions[filledQuiz]!![0].pending).isTrue()
            assertThat(questions[filledQuiz]!![1].question).isEqualTo("Wo ist das?")
            assertThat(questions[filledQuiz]!![1].pending).isTrue()
        }

        eventBus.post(QuestionCreatedEvent(quiz1Id, question5))

        await untilAsserted {
            val questions = questionProjection.determineQuestions()

            assertThat(questions).hasSize(2)

            var emptyQuiz: UUID
            var filledQuiz: UUID

            if (questions[quiz1Id]!!.isEmpty()) {
                emptyQuiz = quiz1Id
                filledQuiz = quiz2Id
            } else {
                emptyQuiz = quiz2Id
                filledQuiz = quiz1Id
            }

            assertThat(questions[emptyQuiz]).isEmpty()
            assertThat(questions[filledQuiz]).hasSize(2)
            assertThat(questions[filledQuiz]!![0].question).isEqualTo("Warum ist das so?")
            assertThat(questions[filledQuiz]!![0].pending).isTrue()
            assertThat(questions[filledQuiz]!![1].question).isEqualTo("Wo ist das?")
            assertThat(questions[filledQuiz]!![1].pending).isTrue()
        }

        eventBus.post(QuestionDeletedEvent(quiz1Id, question5.id))

        await untilAsserted {
            val questions = questionProjection.determineQuestions()

            assertThat(questions).hasSize(2)

            var emptyQuiz: UUID
            var filledQuiz: UUID

            if (questions[quiz1Id]!!.isEmpty()) {
                emptyQuiz = quiz1Id
                filledQuiz = quiz2Id
            } else {
                emptyQuiz = quiz2Id
                filledQuiz = quiz1Id
            }

            assertThat(questions[emptyQuiz]).isEmpty()
            assertThat(questions[filledQuiz]).hasSize(2)
            assertThat(questions[filledQuiz]!![0].question).isEqualTo("Warum ist das so?")
            assertThat(questions[filledQuiz]!![0].pending).isTrue()
            assertThat(questions[filledQuiz]!![1].question).isEqualTo("Wo ist das?")
            assertThat(questions[filledQuiz]!![1].pending).isTrue()
        }
    }

}