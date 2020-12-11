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

internal class QuizProjectionQuizFinishedEventTest {

    @Test
    fun shouldHandleHandleQuizFinishedEventWhenQuizIsNotInCacheAndLastEventWasAlreadyPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")

        val buzzerQuestion = Question(question = "Wofür steht die Abkürzung a.D.?")
        val freetextQuestion = Question(question = "Wer schrieb Peter und der Wolf?", initialTimeToAnswer = 45, estimates = HashMap())
        val choice1 = Choice(choice = "Zugspitze")
        val choice2 = Choice(choice = "Brocken")
        val multipleChoiceQuestion = Question(question = "Was ist der höchste Berg Deutschlands?", choices = listOf(choice1, choice2))
        val participant1 = Participant(name = "Lena")
        val participant2 = Participant(name = "Erik")
        val finishedEvent = QuizFinishedEvent(quiz.id, sequenceNumber = 19)

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, sequenceNumber = 1),
                        QuestionCreatedEvent(quiz.id, buzzerQuestion, sequenceNumber = 2),
                        QuestionCreatedEvent(quiz.id, freetextQuestion, sequenceNumber = 3),
                        QuestionCreatedEvent(quiz.id, multipleChoiceQuestion, sequenceNumber = 4),
                        ParticipantCreatedEvent(quiz.id, participant1, sequenceNumber = 5),
                        ParticipantCreatedEvent(quiz.id, participant2, sequenceNumber = 6),
                        QuestionAskedEvent(quiz.id, buzzerQuestion.id, sequenceNumber = 7),
                        BuzzeredEvent(quiz.id, participant1.id, sequenceNumber = 8),
                        AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, sequenceNumber = 9),
                        QuestionAskedEvent(quiz.id, freetextQuestion.id, sequenceNumber = 10),
                        EstimatedEvent(quiz.id, participant1.id, "Sergej Prokofjew", sequenceNumber = 11),
                        EstimatedEvent(quiz.id, participant2.id, "Max Mustermann", sequenceNumber = 12),
                        AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, sequenceNumber = 13),
                        QuestionAskedEvent(quiz.id, multipleChoiceQuestion.id, sequenceNumber = 14),
                        ChoiceSelectedEvent(quiz.id, participant1.id, choice1.id, sequenceNumber = 15),
                        ChoiceSelectedEvent(quiz.id, participant2.id, choice1.id, sequenceNumber = 16),
                        AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, sequenceNumber = 17),
                        AnsweredEvent(quiz.id, participant2.id, AnswerCommand.Answer.CORRECT, sequenceNumber = 18),
                        finishedEvent
                ))

        val eventBus = EventBus()
        val quizProjection = DefaultQuizProjection(eventBus, eventRepository, UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(finishedEvent)

        await untilAsserted {
            assertThat(observedQuiz.get())
                    .hasId(quiz.id)
                    .undoIsPossible()
                    .isFinished
        }
    }

    @Test
    fun shouldHandleHandleQuizFinishedEventCreationWhenQuizIsNotInCacheAndLastEventWasNotYetPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")

        val buzzerQuestion = Question(question = "Wofür steht die Abkürzung a.D.?")
        val freetextQuestion = Question(question = "Wer schrieb Peter und der Wolf?", initialTimeToAnswer = 45, estimates = HashMap())
        val choice1 = Choice(choice = "Zugspitze")
        val choice2 = Choice(choice = "Brocken")
        val multipleChoiceQuestion = Question(question = "Was ist der höchste Berg Deutschlands?", choices = listOf(choice1, choice2))
        val participant1 = Participant(name = "Lena")
        val participant2 = Participant(name = "Erik")

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, sequenceNumber = 1),
                        QuestionCreatedEvent(quiz.id, buzzerQuestion, sequenceNumber = 2),
                        QuestionCreatedEvent(quiz.id, freetextQuestion, sequenceNumber = 3),
                        QuestionCreatedEvent(quiz.id, multipleChoiceQuestion, sequenceNumber = 4),
                        ParticipantCreatedEvent(quiz.id, participant1, sequenceNumber = 5),
                        ParticipantCreatedEvent(quiz.id, participant2, sequenceNumber = 6),
                        QuestionAskedEvent(quiz.id, buzzerQuestion.id, sequenceNumber = 7),
                        BuzzeredEvent(quiz.id, participant1.id, sequenceNumber = 8),
                        AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, sequenceNumber = 9),
                        QuestionAskedEvent(quiz.id, freetextQuestion.id, sequenceNumber = 10),
                        EstimatedEvent(quiz.id, participant1.id, "Sergej Prokofjew", sequenceNumber = 11),
                        EstimatedEvent(quiz.id, participant2.id, "Max Mustermann", sequenceNumber = 12),
                        AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, sequenceNumber = 13),
                        QuestionAskedEvent(quiz.id, multipleChoiceQuestion.id, sequenceNumber = 14),
                        ChoiceSelectedEvent(quiz.id, participant1.id, choice1.id, sequenceNumber = 15),
                        ChoiceSelectedEvent(quiz.id, participant2.id, choice1.id, sequenceNumber = 16),
                        AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, sequenceNumber = 17),
                        AnsweredEvent(quiz.id, participant2.id, AnswerCommand.Answer.CORRECT, sequenceNumber = 18),
                ))

        val eventBus = EventBus()
        val quizProjection = DefaultQuizProjection(eventBus, eventRepository, UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(QuizFinishedEvent(quiz.id, sequenceNumber = 19))

        await untilAsserted {
            assertThat(observedQuiz.get())
                    .hasId(quiz.id)
                    .undoIsPossible()
                    .isFinished
        }
    }
}