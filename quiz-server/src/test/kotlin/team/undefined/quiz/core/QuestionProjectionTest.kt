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

        val question1 = Question(question = "Warum ist das so?", visibility = Question.QuestionVisibility.PUBLIC, category = QuestionCategory("category1"))
        val question2 = Question(question = "Wo ist das?", visibility = Question.QuestionVisibility.PUBLIC, category = QuestionCategory("category1"))
        val question3 = Question(question = "Wo ist das?", visibility = Question.QuestionVisibility.PUBLIC, category = QuestionCategory("category1"))
        val privateQuestion = Question(question = "Frage mit privatem Inhalt?", category = QuestionCategory("category1"))
        val question4 = Question(question = "Wer ist das?", visibility = Question.QuestionVisibility.PUBLIC, category = QuestionCategory("category1"))
        val question5 = Question(question = "Wie ist das?", visibility = Question.QuestionVisibility.PUBLIC, category = QuestionCategory("category1"))
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
        questionProjection.initializeEvents()

        await untilAsserted {
            val questions = questionProjection.determineQuestions(QuestionCategory("category1"))

            assertThat(questions).hasSize(1)

            val quizId: UUID = if (questions[quiz1Id]?.isEmpty() != false) {
                quiz2Id
            } else {
                quiz1Id
            }

            assertThat(questions[quizId]).hasSize(2)
            assertThat(questions[quizId]!![0].question).isEqualTo("Warum ist das so?")
            assertThat(questions[quizId]!![0].pending).isTrue()
            assertThat(questions[quizId]!![1].question).isEqualTo("Wo ist das?")
            assertThat(questions[quizId]!![1].pending).isTrue()
        }

        eventBus.post(QuestionCreatedEvent(quiz1Id, question5))

        await untilAsserted {
            val questions = questionProjection.determineQuestions(QuestionCategory("category1"))

            assertThat(questions).hasSize(1)

            val quizId: UUID = if (questions[quiz1Id]?.isEmpty() != false) {
                quiz2Id
            } else {
                quiz1Id
            }

            assertThat(questions[quizId]).hasSize(2)
            assertThat(questions[quizId]!![0].question).isEqualTo("Warum ist das so?")
            assertThat(questions[quizId]!![0].pending).isTrue()
            assertThat(questions[quizId]!![1].question).isEqualTo("Wo ist das?")
            assertThat(questions[quizId]!![1].pending).isTrue()
        }

        eventBus.post(QuestionDeletedEvent(quiz1Id, question5.id))

        await untilAsserted {
            val questions = questionProjection.determineQuestions(QuestionCategory("category1"))

            assertThat(questions).hasSize(1)

            val quizId: UUID = if (questions[quiz1Id]?.isEmpty() != false) {
                quiz2Id
            } else {
                quiz1Id
            }

            assertThat(questions[quizId]).hasSize(2)
            assertThat(questions[quizId]!![0].question).isEqualTo("Warum ist das so?")
            assertThat(questions[quizId]!![0].pending).isTrue()
            assertThat(questions[quizId]!![1].question).isEqualTo("Wo ist das?")
            assertThat(questions[quizId]!![1].pending).isTrue()
        }
    }

    @Test
    fun shouldHandleQuestionChange() {
        val quizId = UUID.randomUUID()

        val eventBus = EventBus()

        val question1 = Question(question = "Warum ist das so?", visibility = Question.QuestionVisibility.PUBLIC, category = QuestionCategory("category1"))
        val question2 = Question(question = "Wo ist das?", visibility = Question.QuestionVisibility.PRIVATE, category = QuestionCategory("category1"))
        val question3 = Question(question = "Wie wurde das gemacht", visibility = Question.QuestionVisibility.PRIVATE, category = QuestionCategory("category1"))
        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents()).thenReturn(Flux.just(
                QuizCreatedEvent(quizId, Quiz(name = "Awesome Quiz1")),
                QuestionCreatedEvent(quizId, question1),
                QuestionEditedEvent(quizId, Question(question1.id, question = "Warum ist das so?", visibility = Question.QuestionVisibility.PRIVATE, category = QuestionCategory("category1"))),
                QuestionAskedEvent(quizId, question1.id),
                QuestionCreatedEvent(quizId, question2),
                QuestionEditedEvent(quizId, Question(question2.id, question = "Wo ist das?", imageUrl = "pathToImage", visibility = Question.QuestionVisibility.PUBLIC, category = QuestionCategory("category1"))),
                QuestionAskedEvent(quizId, question2.id),
                QuestionCreatedEvent(quizId, question3),
                QuestionEditedEvent(quizId, Question(question3.id, question = "Wie wurde das gemacht?", visibility = Question.QuestionVisibility.PUBLIC, category = QuestionCategory("category1"))),
                QuestionAskedEvent(quizId, question3.id)
        ))

        val questionProjection = QuestionProjection(eventBus, eventRepository)
        questionProjection.initializeEvents()

        await untilAsserted {
            val questions = questionProjection.determineQuestions(QuestionCategory("category1"))

            assertThat(questions).hasSize(1)

            assertThat(questions[quizId]).hasSize(1)
            assertThat(questions[quizId]!![0].question).isEqualTo("Wie wurde das gemacht?")
            assertThat(questions[quizId]!![0].pending).isTrue()
        }
    }

    @Test
    fun shouldHandleQuizDeletion() {
        val quiz1Id = UUID.randomUUID()
        val quiz2Id = UUID.randomUUID()

        val eventBus = EventBus()

        val question1 = Question(question = "Warum ist das so?", visibility = Question.QuestionVisibility.PUBLIC, category = QuestionCategory("category1"))
        val question2 = Question(question = "Wo ist das?", visibility = Question.QuestionVisibility.PUBLIC, category = QuestionCategory("category1"))
        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents()).thenReturn(Flux.just(
                QuizCreatedEvent(quiz1Id, Quiz(name = "Awesome Quiz1")),
                QuizCreatedEvent(quiz2Id, Quiz(name = "Awesome Quiz2")),
                QuestionCreatedEvent(quiz1Id, question1),
                QuestionAskedEvent(quiz1Id, question1.id),
                QuestionCreatedEvent(quiz2Id, question2),
                QuestionAskedEvent(quiz2Id, question2.id)
        ))

        val questionProjection = QuestionProjection(eventBus, eventRepository)
        questionProjection.initializeEvents()

        await untilAsserted {
            val questions = questionProjection.determineQuestions(QuestionCategory("category1"))

            assertThat(questions).hasSize(2)

            assertThat(questions[quiz1Id]).hasSize(1)
            assertThat(questions[quiz1Id]!![0].question).isEqualTo("Warum ist das so?")
            assertThat(questions[quiz1Id]!![0].pending).isTrue()

            assertThat(questions[quiz2Id]).hasSize(1)
            assertThat(questions[quiz2Id]!![0].question).isEqualTo("Wo ist das?")
            assertThat(questions[quiz2Id]!![0].pending).isTrue()
        }

        eventBus.post(QuizDeletedEvent(quiz2Id))

        await untilAsserted {
            val questions = questionProjection.determineQuestions(QuestionCategory("category1"))

            assertThat(questions).hasSize(1)

            assertThat(questions[quiz1Id]).hasSize(1)
            assertThat(questions[quiz1Id]!![0].question).isEqualTo("Warum ist das so?")
            assertThat(questions[quiz1Id]!![0].pending).isTrue()
        }
    }

    @Test
    fun shouldHandleQuestionCategories() {
        val quiz1Id = UUID.randomUUID()
        val quiz2Id = UUID.randomUUID()

        val eventBus = EventBus()

        val question1 = Question(question = "Warum ist das so?", visibility = Question.QuestionVisibility.PUBLIC, category = QuestionCategory("category1"))
        val question2 = Question(question = "Wo ist das?", visibility = Question.QuestionVisibility.PUBLIC, category = QuestionCategory("category2"))
        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents()).thenReturn(Flux.just(
                QuizCreatedEvent(quiz1Id, Quiz(name = "Awesome Quiz1")),
                QuizCreatedEvent(quiz2Id, Quiz(name = "Awesome Quiz2")),
                QuestionCreatedEvent(quiz1Id, question1),
                QuestionAskedEvent(quiz1Id, question1.id),
                QuestionCreatedEvent(quiz2Id, question2),
                QuestionAskedEvent(quiz2Id, question2.id)
        ))

        val questionProjection = QuestionProjection(eventBus, eventRepository)
        questionProjection.initializeEvents()

        await untilAsserted {
            val questions = questionProjection.determineQuestions(QuestionCategory("category1"))

            assertThat(questions).hasSize(1)

            assertThat(questions[quiz1Id]).hasSize(1)
            assertThat(questions[quiz1Id]!![0].question).isEqualTo("Warum ist das so?")
            assertThat(questions[quiz1Id]!![0].pending).isTrue()
        }

        await untilAsserted {
            val questions = questionProjection.determineQuestions(QuestionCategory("category2"))

            assertThat(questions).hasSize(1)

            assertThat(questions[quiz2Id]).hasSize(1)
            assertThat(questions[quiz2Id]!![0].question).isEqualTo("Wo ist das?")
            assertThat(questions[quiz2Id]!![0].pending).isTrue()
        }
    }

    @Test
    fun shouldHandleRevertOfQuestions() {
        val quiz1Id = UUID.randomUUID()

        val eventBus = EventBus()

        val question1 = Question(question = "Warum ist das so?", visibility = Question.QuestionVisibility.PUBLIC, category = QuestionCategory("category1"))
        val question2 = Question(question = "Wo ist das?", visibility = Question.QuestionVisibility.PUBLIC, category = QuestionCategory("category1"))
        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents()).thenReturn(Flux.just(
                QuizCreatedEvent(quiz1Id, Quiz(name = "Awesome Quiz1")),
                QuestionCreatedEvent(quiz1Id, question1),
                QuestionCreatedEvent(quiz1Id, question2),
                QuestionAskedEvent(quiz1Id, question1.id),
                QuestionAskedEvent(quiz1Id, question2.id),
                QuestionAskedEvent(quiz1Id, question2.id)
        ))

        val questionProjection = QuestionProjection(eventBus, eventRepository)

        await untilAsserted {
            val questions = questionProjection.determineQuestions(QuestionCategory("category1"))

            assertThat(questions).hasSize(1)

            assertThat(questions[quiz1Id]).hasSize(1)
            assertThat(questions[quiz1Id]!![0].question).isEqualTo("Warum ist das so?")
            assertThat(questions[quiz1Id]!![0].pending).isTrue()
        }
    }

}