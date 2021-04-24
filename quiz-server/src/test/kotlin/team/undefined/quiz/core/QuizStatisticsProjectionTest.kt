package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import team.undefined.quiz.core.QuizStatisticsAssert.assertThat

internal class QuizStatisticsProjectionTest {

    @Test
    fun shouldHandleBuzzerWithoutAskedQuestion() {
        val quiz = Quiz(name = "Awesome Quiz")

        val buzzerQuestion = Question(question = "Wofür steht die Abkürzung a.D.?")
        val freetextQuestion =
            Question(question = "Wer schrieb Peter und der Wolf?", initialTimeToAnswer = 45, estimates = HashMap())
        val participant1 = Participant(name = "Lena")
        val participant2 = Participant(name = "Erik")

        val eventRepository = mock(EventRepository::class.java)
        Mockito.`when`(eventRepository.determineEvents(quiz.id))
            .thenReturn(
                Flux.just(
                    QuizCreatedEvent(quiz.id, quiz, 1, 1),
                    QuestionCreatedEvent(quiz.id, buzzerQuestion, 2, 2),
                    QuestionCreatedEvent(quiz.id, freetextQuestion, 3, 3),
                    ParticipantCreatedEvent(quiz.id, participant1, 4, 4),
                    ParticipantCreatedEvent(quiz.id, participant2, 5, 5),
                    BuzzeredEvent(quiz.id, participant1.id, 7, 7),
                    QuestionAskedEvent(quiz.id, buzzerQuestion.id, 8, 8),
                    BuzzeredEvent(quiz.id, participant1.id, 9, 9),
                    AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.INCORRECT, 10, 10),
                    BuzzeredEvent(quiz.id, participant1.id, 11, 11),
                    AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, 12, 12),
                    QuestionAskedEvent(quiz.id, freetextQuestion.id, 13, 13),
                    EstimatedEvent(quiz.id, participant1.id, "Sergej Prokofjew", 14, 14),
                    EstimatedEvent(quiz.id, participant2.id, "Max Mustermann", 15, 15),
                    AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, 16, 16),
                    QuizFinishedEvent(quiz.id, 17, 17)
                )
            )

        val quizStatisticsProvider = QuizStatisticsProjection(
            eventRepository,
            mock(EventBus::class.java),
            QuizStatisticsProjectionConfiguration(25, 1)
        )

        StepVerifier.create(quizStatisticsProvider.determineQuizStatistics(quiz.id))
                .consumeNextWith { quizStatistics ->
                    assertThat(quizStatistics)
                            .participantStatisticsSizeIs(2)
                            .hasParticipantStatistics(0) { participantStatistics ->
                                participantStatistics
                                        .hasParticipantId(participant1.id)
                                        .questionStatisticsSizeIs(2)
                                        .hasQuestionStatistics(0) { questionStatistics ->
                                            questionStatistics
                                                    .hasQuestionId(buzzerQuestion.id)
                                                    .ratingSizeIs(2)
                                                    .hasRating(0, AnswerCommand.Answer.INCORRECT)
                                                    .hasRating(1, AnswerCommand.Answer.CORRECT)
                                        }
                                        .hasQuestionStatistics(1) { questionStatistics ->
                                            questionStatistics
                                                    .hasQuestionId(freetextQuestion.id)
                                                    .ratingSizeIs(1)
                                                    .hasRating(0, AnswerCommand.Answer.CORRECT)
                                        }
                            }
                            .hasParticipantStatistics(1) { participantStatistics ->
                                participantStatistics
                                        .hasParticipantId(participant2.id)
                                        .questionStatisticsSizeIs(2)
                                        .hasQuestionStatistics(0) { questionStatistics ->
                                            questionStatistics
                                                    .hasQuestionId(buzzerQuestion.id)
                                                    .ratingSizeIs(0)
                                        }
                                        .hasQuestionStatistics(1) { questionStatistics ->
                                            questionStatistics
                                                    .hasQuestionId(freetextQuestion.id)
                                                    .ratingSizeIs(0)
                                        }
                            }
                }
                .verifyComplete()
    }

    @Test
    fun shouldHandleDeletionOfQuestions() {
        val quiz = Quiz(name = "Awesome Quiz")

        val buzzerQuestion = Question(question = "Wofür steht die Abkürzung a.D.?")
        val freetextQuestion =
                Question(question = "Wer schrieb Peter und der Wolf?", initialTimeToAnswer = 45, estimates = HashMap())
        val questionToBeDeleted = Question(question = "Ich komme nicht ins Quiz")
        val participant1 = Participant(name = "Lena")
        val participant2 = Participant(name = "Erik")

        val eventRepository = mock(EventRepository::class.java)
        Mockito.`when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(
                        Flux.just(
                                QuizCreatedEvent(quiz.id, quiz, 1, 1),
                                QuestionCreatedEvent(quiz.id, buzzerQuestion, 2, 2),
                                QuestionCreatedEvent(quiz.id, freetextQuestion, 3, 3),
                                QuestionCreatedEvent(quiz.id, questionToBeDeleted, 4, 4),
                                ParticipantCreatedEvent(quiz.id, participant1, 5, 5),
                                ParticipantCreatedEvent(quiz.id, participant2, 6, 6),
                                BuzzeredEvent(quiz.id, participant1.id, 7, 7),
                                QuestionAskedEvent(quiz.id, buzzerQuestion.id, 8, 8),
                                BuzzeredEvent(quiz.id, participant1.id, 9, 9),
                                AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.INCORRECT, 10, 10),
                                BuzzeredEvent(quiz.id, participant1.id, 11, 11),
                                AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, 12, 12),
                                QuestionAskedEvent(quiz.id, freetextQuestion.id, 13, 13),
                                EstimatedEvent(quiz.id, participant1.id, "Sergej Prokofjew", 14, 14),
                                EstimatedEvent(quiz.id, participant2.id, "Max Mustermann", 15, 15),
                                AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, 16, 16),
                                QuestionDeletedEvent(quiz.id, questionToBeDeleted.id, 17, 17),
                                QuizFinishedEvent(quiz.id, 18, 18)
                        )
                )

        val quizStatisticsProvider = QuizStatisticsProjection(
                eventRepository,
                mock(EventBus::class.java),
                QuizStatisticsProjectionConfiguration(25, 1)
        )

        StepVerifier.create(quizStatisticsProvider.determineQuizStatistics(quiz.id))
                .consumeNextWith { quizStatistics ->
                    assertThat(quizStatistics)
                            .participantStatisticsSizeIs(2)
                            .hasParticipantStatistics(0) { participantStatistics ->
                                participantStatistics
                                        .hasParticipantId(participant1.id)
                                        .questionStatisticsSizeIs(2)
                                        .hasQuestionStatistics(0) { questionStatistics ->
                                            questionStatistics
                                                    .hasQuestionId(buzzerQuestion.id)
                                                    .ratingSizeIs(2)
                                                    .hasRating(0, AnswerCommand.Answer.INCORRECT)
                                                    .hasRating(1, AnswerCommand.Answer.CORRECT)
                                        }
                                        .hasQuestionStatistics(1) { questionStatistics ->
                                            questionStatistics
                                                    .hasQuestionId(freetextQuestion.id)
                                                    .ratingSizeIs(1)
                                                    .hasRating(0, AnswerCommand.Answer.CORRECT)
                                        }
                            }
                            .hasParticipantStatistics(1) { participantStatistics ->
                                participantStatistics
                                        .hasParticipantId(participant2.id)
                                        .questionStatisticsSizeIs(2)
                                        .hasQuestionStatistics(0) { questionStatistics ->
                                            questionStatistics
                                                    .hasQuestionId(buzzerQuestion.id)
                                                    .ratingSizeIs(0)
                                        }
                                        .hasQuestionStatistics(1) { questionStatistics ->
                                            questionStatistics
                                                    .hasQuestionId(freetextQuestion.id)
                                                    .ratingSizeIs(0)
                                        }
                            }
                }
                .verifyComplete()
    }

    @Test
    fun shouldHandleEventsAsSoonAsTheyComeIn() {
        val quiz = Quiz(name = "Awesome Quiz")

        val buzzerQuestion = Question(question = "Wofür steht die Abkürzung a.D.?")
        val freetextQuestion =
                Question(question = "Wer schrieb Peter und der Wolf?", initialTimeToAnswer = 45, estimates = HashMap())
        val choice1 = Choice(choice = "Zugspitze")
        val choice2 = Choice(choice = "Brocken")
        val multipleChoiceQuestion =
                Question(question = "Was ist der höchste Berg Deutschlands?", choices = listOf(choice1, choice2))
        val participant1 = Participant(name = "Lena")
        val participant2 = Participant(name = "Erik")

        val eventRepository = mock(EventRepository::class.java)
        val eventBus = EventBus()

        val quizStatisticsProjection = QuizStatisticsProjection(
                eventRepository,
                eventBus,
                QuizStatisticsProjectionConfiguration(25, 1)
        )

        eventBus.post(QuizCreatedEvent(quiz.id, quiz, 1, 1))
        eventBus.post(QuestionCreatedEvent(quiz.id, buzzerQuestion, 2, 2))
        eventBus.post(QuestionCreatedEvent(quiz.id, freetextQuestion, 3, 3))
        eventBus.post(QuestionCreatedEvent(quiz.id, multipleChoiceQuestion, 4, 4))
        eventBus.post(ParticipantCreatedEvent(quiz.id, participant1, 5, 5))
        eventBus.post(ParticipantCreatedEvent(quiz.id, participant2, 6, 6))
        eventBus.post(QuestionAskedEvent(quiz.id, buzzerQuestion.id, 7, 7))
        eventBus.post(BuzzeredEvent(quiz.id, participant1.id, 8, 8))
        eventBus.post(AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, 9, 9))
        eventBus.post(QuestionAskedEvent(quiz.id, freetextQuestion.id, 10, 10))
        eventBus.post(EstimatedEvent(quiz.id, participant1.id, "Sergej Prokofjew", 11, 11))
        eventBus.post(EstimatedEvent(quiz.id, participant2.id, "Max Mustermann", 12, 12))
        eventBus.post(AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, 13, 13))
        eventBus.post(QuestionAskedEvent(quiz.id, multipleChoiceQuestion.id, 14, 14))
        eventBus.post(ChoiceSelectedEvent(quiz.id, participant1.id, choice1.id, 15, 15))
        eventBus.post(ChoiceSelectedEvent(quiz.id, participant2.id, choice1.id, 16, 16))
        eventBus.post(AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, 17, 17))
        eventBus.post(AnsweredEvent(quiz.id, participant2.id, AnswerCommand.Answer.CORRECT, 18, 18))
        eventBus.post(QuizFinishedEvent(quiz.id, 19, 19))

        StepVerifier.create(quizStatisticsProjection.determineQuizStatistics(quiz.id))
                .consumeNextWith { quizStatistics ->
                    assertThat(quizStatistics)
                            .participantStatisticsSizeIs(2)
                            .hasParticipantStatistics(0) { participantStatistics ->
                                participantStatistics
                                        .hasParticipantId(participant1.id)
                                        .questionStatisticsSizeIs(3)
                                        .hasQuestionStatistics(0) { questionStatistics ->
                                            questionStatistics
                                                    .hasQuestionId(buzzerQuestion.id)
                                                    .ratingSizeIs(1)
                                                    .hasRating(0, AnswerCommand.Answer.CORRECT)
                                        }
                                        .hasQuestionStatistics(1) { questionStatistics ->
                                            questionStatistics
                                                    .hasQuestionId(freetextQuestion.id)
                                                    .ratingSizeIs(1)
                                                    .hasRating(0, AnswerCommand.Answer.CORRECT)
                                        }
                                        .hasQuestionStatistics(2) { questionStatistics ->
                                            questionStatistics
                                                    .hasQuestionId(multipleChoiceQuestion.id)
                                                    .ratingSizeIs(1)
                                                    .hasRating(0, AnswerCommand.Answer.CORRECT)
                                        }
                            }
                            .hasParticipantStatistics(1) { participantStatistics ->
                                participantStatistics
                                        .hasParticipantId(participant2.id)
                                        .questionStatisticsSizeIs(3)
                                        .hasQuestionStatistics(0) { questionStatistics ->
                                            questionStatistics
                                                    .hasQuestionId(buzzerQuestion.id)
                                                    .ratingSizeIs(0)
                                        }
                                        .hasQuestionStatistics(1) { questionStatistics ->
                                            questionStatistics
                                                    .hasQuestionId(freetextQuestion.id)
                                                    .ratingSizeIs(0)
                                        }
                                        .hasQuestionStatistics(2) { questionStatistics ->
                                            questionStatistics
                                                    .hasQuestionId(multipleChoiceQuestion.id)
                                                    .ratingSizeIs(1)
                                                    .hasRating(0, AnswerCommand.Answer.CORRECT)
                                        }
                            }
                }
                .verifyComplete()
    }
}