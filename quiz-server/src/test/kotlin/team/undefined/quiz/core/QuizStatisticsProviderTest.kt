package team.undefined.quiz.core

import org.junit.jupiter.api.Test
import org.mockito.Mockito
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import team.undefined.quiz.core.QuizStatisticsAssert.assertThat

internal class QuizStatisticsProviderTest {

    @Test
    fun shouldHandleBuzzerWithoutAskedQuestion() {
        val quiz = Quiz(name = "Awesome Quiz")

        val buzzerQuestion = Question(question = "Wofür steht die Abkürzung a.D.?")
        val freetextQuestion = Question(question = "Wer schrieb Peter und der Wolf?", initialTimeToAnswer = 45, estimates = HashMap())
        val participant1 = Participant(name = "Lena")
        val participant2 = Participant(name = "Erik")

        val eventRepository = Mockito.mock(EventRepository::class.java)
        Mockito.`when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, 1),
                        QuestionCreatedEvent(quiz.id, buzzerQuestion, 2),
                        QuestionCreatedEvent(quiz.id, freetextQuestion, 3),
                        ParticipantCreatedEvent(quiz.id, participant1, 4),
                        ParticipantCreatedEvent(quiz.id, participant2, 5),
                        BuzzeredEvent(quiz.id, participant1.id, 7),
                        QuestionAskedEvent(quiz.id, buzzerQuestion.id, 8),
                        BuzzeredEvent(quiz.id, participant1.id, 9),
                        AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.INCORRECT, 10),
                        BuzzeredEvent(quiz.id, participant1.id, 11),
                        AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, 12),
                        QuestionAskedEvent(quiz.id, freetextQuestion.id, 13),
                        EstimatedEvent(quiz.id, participant1.id, "Sergej Prokofjew", 14),
                        EstimatedEvent(quiz.id, participant2.id, "Max Mustermann", 15),
                        AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, 16),
                        QuizFinishedEvent(quiz.id, 17)
                ))

        val quizStatisticsProvider = QuizStatisticsProvider(eventRepository)

        StepVerifier.create(quizStatisticsProvider.generateStatistics(quiz.id))
                .consumeNextWith { quiz ->
                    assertThat(quiz)
                            .questionStatisticsSizeIs(2)
                            .hasQuestionStatistics(0) { questionStatistics ->
                                questionStatistics
                                        .hasQuestionId(buzzerQuestion.id)
                                        .answerStatisticsSizeIs(2)
                                        .hasAnswerStatistics(0) { it.isEqualTo(AnswerStatistics(participant1.id, 1L)) }
                                        .hasAnswerStatistics(1) { it.isEqualTo(AnswerStatistics(participant1.id, 3L, rating = AnswerCommand.Answer.CORRECT)) }
                            }
                            .hasQuestionStatistics(1) { questionStatistics ->
                                questionStatistics
                                        .hasQuestionId(freetextQuestion.id)
                                        .answerStatisticsSizeIs(2)
                                        .hasAnswerStatistics(0) { it.isEqualTo(AnswerStatistics(participant1.id, 1L, "Sergej Prokofjew", rating = AnswerCommand.Answer.CORRECT)) }
                                        .hasAnswerStatistics(1) { it.isEqualTo(AnswerStatistics(participant2.id, 2L, "Max Mustermann")) }
                            }
                }
                .verifyComplete()
    }

    @Test
    fun shouldHandleDeletionOfQuestions() {
        val quiz = Quiz(name = "Awesome Quiz")

        val buzzerQuestion = Question(question = "Wofür steht die Abkürzung a.D.?")
        val freetextQuestion = Question(question = "Wer schrieb Peter und der Wolf?", initialTimeToAnswer = 45, estimates = HashMap())
        val questionToBeDeleted = Question(question = "Ich komme nicht ins Quiz")
        val participant1 = Participant(name = "Lena")
        val participant2 = Participant(name = "Erik")

        val eventRepository = Mockito.mock(EventRepository::class.java)
        Mockito.`when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, 1),
                        QuestionCreatedEvent(quiz.id, buzzerQuestion, 2),
                        QuestionCreatedEvent(quiz.id, freetextQuestion, 3),
                        QuestionCreatedEvent(quiz.id, questionToBeDeleted, 4),
                        ParticipantCreatedEvent(quiz.id, participant1, 5),
                        ParticipantCreatedEvent(quiz.id, participant2, 6),
                        BuzzeredEvent(quiz.id, participant1.id, 7),
                        QuestionAskedEvent(quiz.id, buzzerQuestion.id, 8),
                        BuzzeredEvent(quiz.id, participant1.id, 9),
                        AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.INCORRECT, 10),
                        BuzzeredEvent(quiz.id, participant1.id, 11),
                        AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, 12),
                        QuestionAskedEvent(quiz.id, freetextQuestion.id, 13),
                        EstimatedEvent(quiz.id, participant1.id, "Sergej Prokofjew", 14),
                        EstimatedEvent(quiz.id, participant2.id, "Max Mustermann", 15),
                        AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, 16),
                        QuestionDeletedEvent(quiz.id, questionToBeDeleted.id, 17),
                        QuizFinishedEvent(quiz.id, 18)
                ))

        val quizStatisticsProvider = QuizStatisticsProvider(eventRepository)

        StepVerifier.create(quizStatisticsProvider.generateStatistics(quiz.id))
                .consumeNextWith { quiz ->
                    assertThat(quiz)
                            .questionStatisticsSizeIs(2)
                            .hasQuestionStatistics(0) { questionStatistics ->
                                questionStatistics
                                        .hasQuestionId(buzzerQuestion.id)
                                        .answerStatisticsSizeIs(2)
                                        .hasAnswerStatistics(0) { it.isEqualTo(AnswerStatistics(participant1.id, 1L)) }
                                        .hasAnswerStatistics(1) { it.isEqualTo(AnswerStatistics(participant1.id, 3L, rating = AnswerCommand.Answer.CORRECT)) }
                            }
                            .hasQuestionStatistics(1) { questionStatistics ->
                                questionStatistics
                                        .hasQuestionId(freetextQuestion.id)
                                        .answerStatisticsSizeIs(2)
                                        .hasAnswerStatistics(0) { it.isEqualTo(AnswerStatistics(participant1.id, 1L, "Sergej Prokofjew", rating = AnswerCommand.Answer.CORRECT)) }
                                        .hasAnswerStatistics(1) { it.isEqualTo(AnswerStatistics(participant2.id, 2L, "Max Mustermann")) }
                            }
                }
                .verifyComplete()
    }

}