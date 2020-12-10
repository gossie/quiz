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
                        QuizCreatedEvent(quizId, Quiz(id = quizId, name = "Awesome Quiz"), 1),
                        QuestionCreatedEvent(quizId, buzzerQuestion, 2),
                        QuestionCreatedEvent(quizId, freetextQuestion, 3),
                        ParticipantCreatedEvent(quizId, participant1, 4),
                        ParticipantCreatedEvent(quizId, participant2, 5),
                        QuestionAskedEvent(quizId, buzzerQuestion.id, 6),
                        BuzzeredEvent(quizId, participant1.id, 7),
                        AnsweredEvent(quizId, participant1.id, AnswerCommand.Answer.CORRECT, 8),
                        QuestionAskedEvent(quizId, freetextQuestion.id, 9),
                        EstimatedEvent(quizId, participant1.id, "Sergej Prokofjew", 10),
                        EstimatedEvent(quizId, participant2.id, "Max Mustermann", 11),
                        AnsweredEvent(quizId, participant1.id, AnswerCommand.Answer.CORRECT, 12)
                ))

        val eventBus = EventBus()
        val quizProjection = DefaultQuizProjection(eventBus, QuizStatisticsProvider(eventRepository), eventRepository, UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quizId)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(QuizCreatedEvent(quizId, Quiz(id = quizId, name = "Awesome Quiz"), 1))
        eventBus.post(QuestionCreatedEvent(quizId, buzzerQuestion, 2))
        eventBus.post(QuestionCreatedEvent(quizId, freetextQuestion, 3))
        eventBus.post(ParticipantCreatedEvent(quizId, participant1, 4))
        eventBus.post(ParticipantCreatedEvent(quizId, participant2, 5))
        eventBus.post(QuestionAskedEvent(quizId, buzzerQuestion.id, 6))
        eventBus.post(BuzzeredEvent(quizId, participant1.id, 7))
        eventBus.post(AnsweredEvent(quizId, participant1.id, AnswerCommand.Answer.CORRECT, 8))
        eventBus.post(QuestionAskedEvent(quizId, freetextQuestion.id, 9))
        eventBus.post(EstimatedEvent(quizId, participant1.id, "Sergej Prokofjew", 10))
        eventBus.post(EstimatedEvent(quizId, participant2.id, "Max Mustermann", 11))
        eventBus.post(AnsweredEvent(quizId, participant1.id, AnswerCommand.Answer.CORRECT, 12))
        eventBus.post(QuizFinishedEvent(quizId, 13))

        await untilAsserted {
            assertThat(observedQuiz.get())
                    .hasId(quizId)
                    .isFinished
                    .hasQuizStatistics { quizStatistics ->
                        quizStatistics
                                .questionStatisticsSizeIs(2)
                                .hasQuestionStatistics(0) { questionStatistics ->
                                    questionStatistics
                                            .hasQuestionId(buzzerQuestion.id)
                                            .answerStatisticsSizeIs(1)
                                            .hasAnswerStatistics(0) { buzzerStatistics ->
                                                buzzerStatistics
                                                        .hasDuration(1L)
                                                        .hasParticipantId(participant1.id)
                                                        .isCorrect
                                            }
                                }
                                .hasQuestionStatistics(1) { questionStatistics ->
                                    questionStatistics
                                            .hasQuestionId(freetextQuestion.id)
                                            .answerStatisticsSizeIs(2)
                                            .hasAnswerStatistics(0) { buzzerStatistics ->
                                                buzzerStatistics
                                                        .hasDuration(1L)
                                                        .hasParticipantId(participant1.id)
                                                        .isCorrect
                                            }
                                            .hasAnswerStatistics(1) { buzzerStatistics ->
                                                buzzerStatistics
                                                        .hasDuration(2L)
                                                        .hasParticipantId(participant2.id)
                                                        .isIncorrect
                                            }
                                }
                    }
        }

        eventBus.post(ReloadQuizCommand(quizId))

        await untilAsserted {
            assertThat(observedQuiz.get())
                    .hasId(quizId)
                    .isNotFinished
                    .hasNoQuizStatistics()
        }
    }
}