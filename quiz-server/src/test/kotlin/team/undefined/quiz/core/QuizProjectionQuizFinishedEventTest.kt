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
        val freetextQuestion = Question(question = "Wer schrieb Peter und der Wolf?", estimates = HashMap(), initialTimeToAnswer = 45)
        val participant1 = Participant(name = "Lena")
        val participant2 = Participant(name = "Erik")
        val finishedEvent = QuizFinishedEvent(quiz.id, 13)

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, 1),
                        QuestionCreatedEvent(quiz.id, buzzerQuestion, 2),
                        QuestionCreatedEvent(quiz.id, freetextQuestion, 3),
                        ParticipantCreatedEvent(quiz.id, participant1, 4),
                        ParticipantCreatedEvent(quiz.id, participant2, 5),
                        QuestionAskedEvent(quiz.id, buzzerQuestion.id, 6),
                        BuzzeredEvent(quiz.id, participant1.id, 7),
                        AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, 8),
                        QuestionAskedEvent(quiz.id, freetextQuestion.id, 9),
                        EstimatedEvent(quiz.id, participant1.id, "Sergej Prokofjew", 10),
                        EstimatedEvent(quiz.id, participant2.id, "Max Mustermann", 11),
                        AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, 12),
                        finishedEvent
                ))

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, QuizStatisticsProvider(eventRepository), eventRepository)

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(finishedEvent)

        await untilAsserted {
            assertThat(observedQuiz.get())
                    .hasId(quiz.id)
                    .isFinished
                    .hasQuizStatistics { quizStatistics ->
                        quizStatistics
                                .questionStatisticsSizeId(2)
                                .hasQuestionStatistics(0) { questionStatistics ->
                                    questionStatistics
                                            .hasQuestionId(buzzerQuestion.id)
                                            .buzzerStatisticsSizeIs(1)
                                            .hasBuzzerStatistics(0) { buzzerStatistics ->
                                                buzzerStatistics
                                                        .hasDuration(1L)
                                                        .hasParticipantId(participant1.id)
                                                        .isCorrect
                                            }
                                }
                                .hasQuestionStatistics(1) { questionStatistics ->
                                    questionStatistics
                                            .hasQuestionId(freetextQuestion.id)
                                            .buzzerStatisticsSizeIs(2)
                                            .hasBuzzerStatistics(0) { buzzerStatistics ->
                                                buzzerStatistics
                                                        .hasDuration(1L)
                                                        .hasParticipantId(participant1.id)
                                                        .isCorrect
                                            }
                                            .hasBuzzerStatistics(1) { buzzerStatistics ->
                                                buzzerStatistics
                                                        .hasDuration(2L)
                                                        .hasParticipantId(participant2.id)
                                                        .isIncorrect
                                            }
                                }
                    }
        }
    }

    @Test
    fun shouldHandleHandleQuizFinishedEventCreationWhenQuizIsNotInCacheAndLastEventWasNotYetPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")

        val buzzerQuestion = Question(question = "Wofür steht die Abkürzung a.D.?")
        val freetextQuestion = Question(question = "Wer schrieb Peter und der Wolf?", estimates = HashMap(), initialTimeToAnswer = 45)
        val participant1 = Participant(name = "Lena")
        val participant2 = Participant(name = "Erik")

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, 1),
                        QuestionCreatedEvent(quiz.id, buzzerQuestion, 2),
                        QuestionCreatedEvent(quiz.id, freetextQuestion, 3),
                        ParticipantCreatedEvent(quiz.id, participant1, 4),
                        ParticipantCreatedEvent(quiz.id, participant2, 5),
                        QuestionAskedEvent(quiz.id, buzzerQuestion.id, 6),
                        BuzzeredEvent(quiz.id, participant1.id, 7),
                        AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, 8),
                        QuestionAskedEvent(quiz.id, freetextQuestion.id, 9),
                        EstimatedEvent(quiz.id, participant1.id, "Sergej Prokofjew", 10),
                        EstimatedEvent(quiz.id, participant2.id, "Max Mustermann", 11),
                        AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, 12)
                ))

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, QuizStatisticsProvider(eventRepository), eventRepository)

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(QuizFinishedEvent(quiz.id, 13))

        await untilAsserted {
            assertThat(observedQuiz.get())
                    .hasId(quiz.id)
                    .isFinished
                    .hasQuizStatistics { quizStatistics ->
                        quizStatistics
                                .questionStatisticsSizeId(2)
                                .hasQuestionStatistics(0) { questionStatistics ->
                                    questionStatistics
                                            .hasQuestionId(buzzerQuestion.id)
                                            .buzzerStatisticsSizeIs(1)
                                            .hasBuzzerStatistics(0) { buzzerStatistics ->
                                                buzzerStatistics
                                                        .hasDuration(1L)
                                                        .hasParticipantId(participant1.id)
                                                        .isCorrect
                                            }
                                }
                                .hasQuestionStatistics(1) { questionStatistics ->
                                    questionStatistics
                                            .hasQuestionId(freetextQuestion.id)
                                            .buzzerStatisticsSizeIs(2)
                                            .hasBuzzerStatistics(0) { buzzerStatistics ->
                                                buzzerStatistics
                                                        .hasDuration(1L)
                                                        .hasParticipantId(participant1.id)
                                                        .isCorrect
                                            }
                                            .hasBuzzerStatistics(1) { buzzerStatistics ->
                                                buzzerStatistics
                                                        .hasDuration(2L)
                                                        .hasParticipantId(participant2.id)
                                                        .isIncorrect
                                            }
                                }
                    }
        }
    }

}