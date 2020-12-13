package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import reactor.core.publisher.Flux
import team.undefined.quiz.core.QuizAssert.assertThat
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.collections.HashMap

internal class QuizProjectionQuizReloadCommandTest {

    @Test
    fun shouldReloadQuizFromDB() {
        val quizId = UUID.randomUUID()

        val buzzerQuestion = Question(question = "Wofür steht die Abkürzung a.D.?")
        val freetextQuestion = Question(question = "Wer schrieb Peter und der Wolf?", initialTimeToAnswer = 45, estimates = HashMap())
        val participant1 = Participant(name = "Lena")
        val participant2 = Participant(name = "Erik")

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quizId))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(
                            quizId, Quiz(
                                id = quizId,
                                name = "Awesome Quiz",
                            ), sequenceNumber = 1
                        ),
                        QuestionCreatedEvent(quizId, buzzerQuestion, sequenceNumber = 2),
                        QuestionCreatedEvent(quizId, freetextQuestion, sequenceNumber = 3),
                        ParticipantCreatedEvent(quizId, participant1, sequenceNumber = 4),
                        ParticipantCreatedEvent(quizId, participant2, sequenceNumber = 5),
                        QuestionAskedEvent(quizId, buzzerQuestion.id, sequenceNumber = 6),
                        BuzzeredEvent(quizId, participant1.id, sequenceNumber = 7),
                        AnsweredEvent(quizId, participant1.id, AnswerCommand.Answer.CORRECT, sequenceNumber = 8),
                        QuestionAskedEvent(quizId, freetextQuestion.id, sequenceNumber = 9),
                        EstimatedEvent(quizId, participant1.id, "Sergej Prokofjew", sequenceNumber = 10),
                        EstimatedEvent(quizId, participant2.id, "Max Mustermann", sequenceNumber = 11),
                        AnsweredEvent(quizId, participant1.id, AnswerCommand.Answer.CORRECT, sequenceNumber = 12)
                ))

        val eventBus = EventBus()
        val quizProjection = DefaultQuizProjection(
            eventBus,
            eventRepository,
            UndoneEventsCache(),
            QuizProjectionConfiguration(25)
        )

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quizId)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(QuizCreatedEvent(quizId, Quiz(id = quizId, name = "Awesome Quiz"), sequenceNumber = 1))
        eventBus.post(QuestionCreatedEvent(quizId, buzzerQuestion, sequenceNumber = 2))
        eventBus.post(QuestionCreatedEvent(quizId, freetextQuestion, sequenceNumber = 3))
        eventBus.post(ParticipantCreatedEvent(quizId, participant1, sequenceNumber = 4))
        eventBus.post(ParticipantCreatedEvent(quizId, participant2, sequenceNumber = 5))
        eventBus.post(QuestionAskedEvent(quizId, buzzerQuestion.id, sequenceNumber = 6))
        eventBus.post(BuzzeredEvent(quizId, participant1.id, sequenceNumber = 7))
        eventBus.post(AnsweredEvent(quizId, participant1.id, AnswerCommand.Answer.CORRECT, sequenceNumber = 8))
        eventBus.post(QuestionAskedEvent(quizId, freetextQuestion.id, sequenceNumber = 9))
        eventBus.post(EstimatedEvent(quizId, participant1.id, "Sergej Prokofjew", sequenceNumber = 10))
        eventBus.post(EstimatedEvent(quizId, participant2.id, "Max Mustermann", sequenceNumber = 11))
        eventBus.post(AnsweredEvent(quizId, participant1.id, AnswerCommand.Answer.CORRECT, sequenceNumber = 12))
        eventBus.post(QuizFinishedEvent(quizId, sequenceNumber = 13))

        await untilAsserted {
            assertThat(observedQuiz.get())
                    .hasId(quizId)
                    .isFinished
        }

        eventBus.post(ReloadQuizCommand(quizId))

        await untilAsserted {
            assertThat(observedQuiz.get())
                    .hasId(quizId)
                    .isNotFinished
        }
    }
}