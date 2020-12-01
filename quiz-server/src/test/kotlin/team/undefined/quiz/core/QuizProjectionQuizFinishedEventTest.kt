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
        val finishedEvent = QuizFinishedEvent(quiz.id, 19)

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, 1),
                        QuestionCreatedEvent(quiz.id, buzzerQuestion, 2),
                        QuestionCreatedEvent(quiz.id, freetextQuestion, 3),
                        QuestionCreatedEvent(quiz.id, multipleChoiceQuestion, 4),
                        ParticipantCreatedEvent(quiz.id, participant1, 5),
                        ParticipantCreatedEvent(quiz.id, participant2, 6),
                        QuestionAskedEvent(quiz.id, buzzerQuestion.id, 7),
                        BuzzeredEvent(quiz.id, participant1.id, 8),
                        AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, 9),
                        QuestionAskedEvent(quiz.id, freetextQuestion.id, 10),
                        EstimatedEvent(quiz.id, participant1.id, "Sergej Prokofjew", 11),
                        EstimatedEvent(quiz.id, participant2.id, "Max Mustermann", 12),
                        AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, 13),
                        QuestionAskedEvent(quiz.id, multipleChoiceQuestion.id, 14),
                        ChoiceSelectedEvent(quiz.id, participant1.id, choice1.id, 15),
                        ChoiceSelectedEvent(quiz.id, participant2.id, choice1.id, 16),
                        AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, 17),
                        AnsweredEvent(quiz.id, participant2.id, AnswerCommand.Answer.CORRECT, 18),
                        finishedEvent
                ))

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, QuizStatisticsProvider(eventRepository), eventRepository, UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(finishedEvent)

        await untilAsserted {
            assertThat(observedQuiz.get())
                    .hasId(quiz.id)
                    .undoIsPossible()
                    .isFinished
                    .hasQuizStatistics { quizStatistics ->
                        quizStatistics
                                .questionStatisticsSizeIs(3)
                                .hasQuestionStatistics(0) { questionStatistics ->
                                    questionStatistics
                                            .hasQuestionId(buzzerQuestion.id)
                                            .answerStatisticsSizeIs(1)
                                            .hasAnswerStatistics(0) { answerStatistics ->
                                                answerStatistics
                                                        .hasDuration(1L)
                                                        .hasParticipantId(participant1.id)
                                                        .isCorrect
                                            }
                                }
                                .hasQuestionStatistics(1) { questionStatistics ->
                                    questionStatistics
                                            .hasQuestionId(freetextQuestion.id)
                                            .answerStatisticsSizeIs(2)
                                            .hasAnswerStatistics(0) { answerStatistics ->
                                                answerStatistics
                                                        .hasDuration(1L)
                                                        .hasParticipantId(participant1.id)
                                                        .hasAnswer("Sergej Prokofjew")
                                                        .isCorrect
                                            }
                                            .hasAnswerStatistics(1) { answerStatistics ->
                                                answerStatistics
                                                        .hasDuration(2L)
                                                        .hasParticipantId(participant2.id)
                                                        .hasAnswer("Max Mustermann")
                                                        .isIncorrect
                                            }
                                }
                                .hasQuestionStatistics(2) { questionStatistics ->
                                    questionStatistics
                                            .hasQuestionId(multipleChoiceQuestion.id)
                                            .answerStatisticsSizeIs(2)
                                            .hasAnswerStatistics(0) { answerStatistics ->
                                                answerStatistics
                                                        .hasDuration(1L)
                                                        .hasParticipantId(participant1.id)
                                                        .hasChoiceId(choice1.id)
                                                        .isCorrect
                                            }
                                            .hasAnswerStatistics(1) { answerStatistics ->
                                                answerStatistics
                                                        .hasDuration(2L)
                                                        .hasParticipantId(participant2.id)
                                                        .hasChoiceId(choice1.id)
                                                        .isCorrect
                                            }
                                }
                    }
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
                        QuizCreatedEvent(quiz.id, quiz, 1),
                        QuestionCreatedEvent(quiz.id, buzzerQuestion, 2),
                        QuestionCreatedEvent(quiz.id, freetextQuestion, 3),
                        QuestionCreatedEvent(quiz.id, multipleChoiceQuestion, 4),
                        ParticipantCreatedEvent(quiz.id, participant1, 5),
                        ParticipantCreatedEvent(quiz.id, participant2, 6),
                        QuestionAskedEvent(quiz.id, buzzerQuestion.id, 7),
                        BuzzeredEvent(quiz.id, participant1.id, 8),
                        AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, 9),
                        QuestionAskedEvent(quiz.id, freetextQuestion.id, 10),
                        EstimatedEvent(quiz.id, participant1.id, "Sergej Prokofjew", 11),
                        EstimatedEvent(quiz.id, participant2.id, "Max Mustermann", 12),
                        AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, 13),
                        QuestionAskedEvent(quiz.id, multipleChoiceQuestion.id, 14),
                        ChoiceSelectedEvent(quiz.id, participant1.id, choice1.id, 15),
                        ChoiceSelectedEvent(quiz.id, participant2.id, choice1.id, 16),
                        AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, 17),
                        AnsweredEvent(quiz.id, participant2.id, AnswerCommand.Answer.CORRECT, 18),
                ))

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, QuizStatisticsProvider(eventRepository), eventRepository, UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(QuizFinishedEvent(quiz.id, 19))

        await untilAsserted {
            assertThat(observedQuiz.get())
                    .hasId(quiz.id)
                    .undoIsPossible()
                    .isFinished
                    .hasQuizStatistics { quizStatistics ->
                        quizStatistics
                                .questionStatisticsSizeIs(3)
                                .hasQuestionStatistics(0) { questionStatistics ->
                                    questionStatistics
                                            .hasQuestionId(buzzerQuestion.id)
                                            .answerStatisticsSizeIs(1)
                                            .hasAnswerStatistics(0) { answerStatistics ->
                                                answerStatistics
                                                        .hasDuration(1L)
                                                        .hasParticipantId(participant1.id)
                                                        .isCorrect
                                            }
                                }
                                .hasQuestionStatistics(1) { questionStatistics ->
                                    questionStatistics
                                            .hasQuestionId(freetextQuestion.id)
                                            .answerStatisticsSizeIs(2)
                                            .hasAnswerStatistics(0) { answerStatistics ->
                                                answerStatistics
                                                        .hasDuration(1L)
                                                        .hasParticipantId(participant1.id)
                                                        .hasAnswer("Sergej Prokofjew")
                                                        .isCorrect
                                            }
                                            .hasAnswerStatistics(1) { answerStatistics ->
                                                answerStatistics
                                                        .hasDuration(2L)
                                                        .hasParticipantId(participant2.id)
                                                        .hasAnswer("Max Mustermann")
                                                        .isIncorrect
                                            }
                                }
                                .hasQuestionStatistics(2) { questionStatistics ->
                                    questionStatistics
                                            .hasQuestionId(multipleChoiceQuestion.id)
                                            .answerStatisticsSizeIs(2)
                                            .hasAnswerStatistics(0) { answerStatistics ->
                                                answerStatistics
                                                        .hasDuration(1L)
                                                        .hasParticipantId(participant1.id)
                                                        .hasChoiceId(choice1.id)
                                                        .isCorrect
                                            }
                                            .hasAnswerStatistics(1) { answerStatistics ->
                                                answerStatistics
                                                        .hasDuration(2L)
                                                        .hasParticipantId(participant2.id)
                                                        .hasChoiceId(choice1.id)
                                                        .isCorrect
                                            }
                                }
                    }
        }
    }
}