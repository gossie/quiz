package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import team.undefined.quiz.core.QuizAssert.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.ignoreException
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.*
import kotlin.collections.HashMap

internal class DefaultQuizServiceTest {

    open class TestEventRepository : EventRepository {
        override fun storeEvent(event: Event): Mono<Event> {
            return Mono.just(event)
        }

        override fun determineEvents(quizId: UUID): Flux<Event> {
            return Flux.empty()
        }

        override fun determineEvents(): Flux<Event> {
            return Flux.empty()
        }

        override fun deleteEvents(quizId: UUID): Mono<Unit> {
            return Mono.just(Unit)
        }

        override fun determineQuizIds(): Flux<UUID> {
            return Flux.empty()
        }

        override fun undoLastAction(quizId: UUID): Mono<Event> {
            return Mono.empty()
        }
    }

    private val quizRepository = spy(TestEventRepository())

    private val eventBus = mock(EventBus::class.java)

    @Test
    fun shouldCreateQuiz() {
        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)
        val quizId = UUID.randomUUID()
        val quiz = Quiz(quizId, "Quiz")
        StepVerifier.create(quizService.createQuiz(CreateQuizCommand(quizId, quiz)))
                .consumeNextWith {
                    verify(eventBus).post(argThat { (it as QuizCreatedEvent).quizId == quizId && it.quiz == quiz })
                }
                .verifyComplete()
    }

    @Test
    fun shouldCreateParticipant() {
        val quizId = UUID.randomUUID()

        `when`(quizRepository.determineEvents(quizId))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Quiz"))
                ))

        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        val participant = Participant(UUID.randomUUID(), "Lena")
        val mono = quizService.createParticipant(CreateParticipantCommand(quizId, participant))
        verify(eventBus).post(ForceEmitCommand(quizId))
        StepVerifier.create(mono)
                .consumeNextWith {
                    verify(eventBus).post( argThat {
                        it is ParticipantCreatedEvent && it.quizId == quizId && it.participant == participant }
                    )
                }
                .verifyComplete()
    }

    @Test
    fun shouldNotCreateParticipantWithTheSameName() {
        val quizId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        `when`(quizRepository.determineEvents(quizId))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Quiz")),
                        ParticipantCreatedEvent(quizId, Participant(participantId, "Sandra"))
                ))

        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        val participant = Participant(name = "Sandra")
        StepVerifier.create(quizService.createParticipant(CreateParticipantCommand(quizId, participant)))
                .verifyComplete()

        verify(eventBus).post(ForceEmitCommand(quizId))
        verifyNoMoreInteractions(eventBus)
    }

    @Test
    fun shouldDeleteParticipant() {
        val quizId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        `when`(quizRepository.determineEvents(quizId))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Quiz")),
                        ParticipantCreatedEvent(quizId, Participant(participantId, "Lena"))
                ))

        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.deleteParticipant(DeleteParticipantCommand(quizId, participantId)))
                .consumeNextWith {
                    verify(eventBus).post( argThat {
                        it is ParticipantDeletedEvent && it.quizId == quizId && it.participantId == participantId }
                    )
                }
                .verifyComplete()
    }

    @Test
    fun shouldNotDeleteParticipantBecauseItDoesNotExist() {
        val quizId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        `when`(quizRepository.determineEvents(quizId))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Quiz"))
                ))

        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.deleteParticipant(DeleteParticipantCommand(quizId, participantId)))
                .verifyComplete()

        verifyNoInteractions(eventBus)
    }

    @Test
    fun shouldBuzzer() {
        val quizId = UUID.randomUUID()
        val andresId = UUID.randomUUID()
        val lenasId = UUID.randomUUID()
        val questionId = UUID.randomUUID()

        `when`(quizRepository.determineEvents(quizId))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Quiz")),
                        QuestionCreatedEvent(quizId, question = Question(questionId, "Warum ist die Banane krum?")),
                        ParticipantCreatedEvent(quizId, Participant(andresId, "André")),
                        ParticipantCreatedEvent(quizId, Participant(lenasId, "Lena")),
                        QuestionAskedEvent(quizId, questionId)
                ))

        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        val buzzer = quizService.buzzer(BuzzerCommand(quizId, lenasId))
        StepVerifier.create(buzzer)
                .consumeNextWith {
                    verify(eventBus).post(argThat { (it as BuzzeredEvent).quizId == quizId && it.participantId == lenasId })
                }
                .verifyComplete()
    }

    @Test
    fun shouldNotAllowBuzzerBecauseItIsAnEstimationQuestion() {
        val quizId = UUID.randomUUID()
        val andresId = UUID.randomUUID()
        val lenasId = UUID.randomUUID()
        val questionId = UUID.randomUUID()

        `when`(quizRepository.determineEvents(quizId))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Quiz")),
                        QuestionCreatedEvent(quizId, question = Question(questionId, "Warum ist die Banane krum?", estimates = HashMap())),
                        ParticipantCreatedEvent(quizId, Participant(andresId, "André")),
                        ParticipantCreatedEvent(quizId, Participant(lenasId, "Lena")),
                        QuestionAskedEvent(quizId, questionId)
                ))

        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        val buzzer = quizService.buzzer(BuzzerCommand(quizId, lenasId))
        StepVerifier.create(buzzer)
                .verifyComplete()

        verifyNoInteractions(eventBus)
    }

    @Test
    fun shouldNotAllowEstimationCommandForBuzzerQuestion() {
        // The Beuke Bug
        val quizId = UUID.randomUUID()
        val beukesId = UUID.randomUUID()
        val questionId = UUID.randomUUID()

        `when`(quizRepository.determineEvents(quizId))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Quiz")),
                        QuestionCreatedEvent(quizId, question = Question(questionId, "Warum ist die Banane krum?")),
                        ParticipantCreatedEvent(quizId, Participant(beukesId, "Beuke")),
                        QuestionAskedEvent(quizId, questionId)
                ))

        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        val buzzer = quizService.estimate(EstimationCommand(quizId, beukesId, "32"))
        StepVerifier.create(buzzer)
                .verifyComplete()

        verifyNoInteractions(eventBus)
    }

    @Test
    fun shouldNotAllowSecondBuzzer() {
        val quizId = UUID.randomUUID()
        val andresId = UUID.randomUUID()
        val lenasId = UUID.randomUUID()
        val questionId = UUID.randomUUID()

        `when`(quizRepository.determineEvents(quizId))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Quiz")),
                        QuestionCreatedEvent(quizId, question = Question(questionId, "Warum ist die Banane krum?")),
                        ParticipantCreatedEvent(quizId, Participant(andresId, "André")),
                        ParticipantCreatedEvent(quizId, Participant(lenasId, "Lena")),
                        QuestionAskedEvent(quizId, questionId),
                        BuzzeredEvent(quizId, lenasId)
                ))

        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        val buzzer = quizService.buzzer(BuzzerCommand(quizId, andresId))
        StepVerifier.create(buzzer)
                .verifyComplete()

        verifyNoInteractions(eventBus)
    }

    @Test
    fun shouldNotBuzzerBecauseParticipantDoesNotExist() {
        val quizId = UUID.randomUUID()
        val andresId = UUID.randomUUID()
        val lenasId = UUID.randomUUID()
        val questionId = UUID.randomUUID()

        `when`(quizRepository.determineEvents(quizId))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Quiz")),
                        QuestionCreatedEvent(quizId, question = Question(questionId, "Warum ist die Banane krum?")),
                        ParticipantCreatedEvent(quizId, Participant(lenasId, "Lena")),
                        QuestionAskedEvent(quizId, questionId)
                ))

        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        val buzzer = quizService.buzzer(BuzzerCommand(quizId, andresId))
        StepVerifier.create(buzzer)
                .verifyComplete()

        verifyNoInteractions(eventBus)
    }

    @Test
    fun shouldCreatePrivateQuestion() {
        val quizId = UUID.randomUUID()

        `when`(quizRepository.determineEvents(quizId))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Quiz"))
                ))

        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        val questionId = UUID.randomUUID()
        StepVerifier.create(quizService.createQuestion(CreateQuestionCommand(quizId, Question(questionId, "Warum ist die Banane krum?"))))
                .consumeNextWith {
                    verify(eventBus).post(argThat { (it as QuestionCreatedEvent).quizId == quizId && it.question == Question(questionId, "Warum ist die Banane krum?", false, "", visibility = Question.QuestionVisibility.PRIVATE, alreadyPlayed = false) })
                }
                .verifyComplete()
    }

    @Test
    fun shouldCreatePublicQuestion() {
        val quizId = UUID.randomUUID()

        `when`(quizRepository.determineEvents(quizId))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Quiz"))
                ))

        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        val questionId = UUID.randomUUID()
        StepVerifier.create(quizService.createQuestion(CreateQuestionCommand(quizId, Question(questionId, "Warum ist die Banane krum?", visibility = Question.QuestionVisibility.PUBLIC))))
                .consumeNextWith {
                    verify(eventBus).post(argThat { (it as QuestionCreatedEvent).quizId == quizId && it.question == Question(questionId, "Warum ist die Banane krum?", false, "", visibility = Question.QuestionVisibility.PUBLIC, alreadyPlayed = false) })
                }
                .verifyComplete()
    }

    @Test
    fun shouldEditQuestionQuestion() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()

        `when`(quizRepository.determineEvents(quizId))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Quiz")),
                        QuestionCreatedEvent(quizId, Question(questionId, question = "Wer ist das", visibility = Question.QuestionVisibility.PRIVATE))
                ))

        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.editQuestion(EditQuestionCommand(quizId, questionId, Question(questionId,"Wer ist das?", imageUrl = "urlToImage", visibility = Question.QuestionVisibility.PUBLIC))))
                .consumeNextWith {
                    verify(eventBus).post(argThat { (it as QuestionEditedEvent).quizId == quizId && it.question == Question(questionId, "Wer ist das?", false, "urlToImage", visibility = Question.QuestionVisibility.PUBLIC, alreadyPlayed = false) })
                }
                .verifyComplete()
    }

    @Test
    fun shouldDeleteQuestion() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()

        `when`(quizRepository.determineEvents(quizId))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Quiz")),
                        QuestionCreatedEvent(quizId, Question(questionId, "Warum ist die Banane krum?"))
                ))

        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.deleteQuestion(DeleteQuestionCommand(quizId, questionId)))
                .consumeNextWith {
                    verify(eventBus).post(argThat { (it as QuestionDeletedEvent).quizId == quizId && it.questionId == questionId })
                }
                .verifyComplete()
    }

    @Test
    fun shouldStartNewQuestion() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()

        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.startNewQuestion(AskQuestionCommand(quizId, questionId)))
                .consumeNextWith {
                    verify(eventBus).post(argThat { (it as QuestionAskedEvent).quizId == quizId && it.questionId == questionId })
                }
                .verifyComplete()
    }

    @Test
    fun shouldStartNewQuestionWithTimeConstaint() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()

        `when`(quizRepository.determineEvents(quizId))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Quiz")),
                        QuestionCreatedEvent(quizId, Question(questionId, "Warum ist die Banane krum?", initialTimeToAnswer = 3, secondsLeft = 3)),
                        QuestionAskedEvent(quizId, questionId)
                ))

        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.startNewQuestion(AskQuestionCommand(quizId, questionId)))
                .consumeNextWith {
                    verify(eventBus).post(argThat { (it as QuestionAskedEvent).quizId == quizId && it.questionId == questionId })
                }
                .verifyComplete()

        await ignoreException ClassCastException::class untilAsserted  {
            verify(eventBus, times(3)).post(argThat {
                it is TimeToAnswerDecreasedEvent
                        && it.quizId == quizId
                        && it.questionId == questionId
            })
        }
    }

    @Test
    fun shouldEstimate() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        `when`(quizRepository.determineEvents(quizId))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Quiz")),
                        QuestionCreatedEvent(quizId, question = Question(questionId, "Warum ist die Banane krum?", estimates = HashMap())),
                        ParticipantCreatedEvent(quizId, Participant(participantId, "Lena")),
                        QuestionAskedEvent(quizId, questionId)
                ))

        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.estimate(EstimationCommand(quizId, participantId, "myEstimatedValue")))
                .consumeNextWith {
                    verify(eventBus).post(argThat { (it as EstimatedEvent).quizId == quizId && it.participantId == participantId && it.estimatedValue == "myEstimatedValue" })
                }
                .verifyComplete()
    }

    @Test
    fun shouldPreventDuplicateEstimates() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        `when`(quizRepository.determineEvents(quizId))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Quiz")),
                        QuestionCreatedEvent(quizId, question = Question(questionId, "Warum ist die Banane krum?", estimates = HashMap())),
                        ParticipantCreatedEvent(quizId, Participant(participantId, "Lena")),
                        QuestionAskedEvent(quizId, questionId),
                        EstimatedEvent(quizId, participantId, "Darum")
                ))

        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.estimate(EstimationCommand(quizId, participantId, "Darum")))
                .verifyComplete()

        verifyNoInteractions(eventBus)
    }

    @Test
    fun shouldPreventAllowMultipleAnswersThatDiffer() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        `when`(quizRepository.determineEvents(quizId))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Quiz")),
                        QuestionCreatedEvent(quizId, question = Question(questionId, "Warum ist die Banane krum?", estimates = HashMap())),
                        ParticipantCreatedEvent(quizId, Participant(participantId, "Lena")),
                        QuestionAskedEvent(quizId, questionId),
                        EstimatedEvent(quizId, participantId, "Darum")
                ))

        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.estimate(EstimationCommand(quizId, participantId, "Oder?")))
                .consumeNextWith {
                    verify(eventBus).post(argThat { (it as EstimatedEvent).quizId == quizId && it.participantId == participantId && it.estimatedValue == "Oder?" })
                }
                .verifyComplete()
    }

    @Test
    fun shouldNotEstimateBecauseParticipantDoesNotExist() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        `when`(quizRepository.determineEvents(quizId))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Quiz")),
                        QuestionCreatedEvent(quizId, question = Question(questionId, "Warum ist die Banane krum?")),
                        QuestionAskedEvent(quizId, questionId)
                ))

        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.estimate(EstimationCommand(quizId, participantId, "myEstimatedValue")))
                .verifyComplete()

        verifyNoInteractions(eventBus)
    }

    @Test
    fun shouldPreventReveal() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        `when`(quizRepository.determineEvents(quizId))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Quiz")),
                        QuestionCreatedEvent(quizId, question = Question(questionId, "Warum ist die Banane krum?")),
                        ParticipantCreatedEvent(quizId, Participant(participantId, "Lena")),
                        QuestionAskedEvent(quizId, questionId)
                ))

        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.toggleAnswerRevealAllowed(ToggleAnswerRevealAllowedCommand(quizId, participantId)))
                .consumeNextWith {
                    verify(eventBus).post(argThat { (it as ToggleAnswerRevealAllowedEvent).quizId == quizId && it.participantId == participantId })
                }
                .verifyComplete()
    }

    @Test
    fun shoulNotPreventRevealBecauseParticipantDoesNotExist() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()
        val participant = UUID.randomUUID()

        `when`(quizRepository.determineEvents(quizId))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Quiz")),
                        QuestionCreatedEvent(quizId, question = Question(questionId, "Warum ist die Banane krum?")),
                        QuestionAskedEvent(quizId, questionId)
                ))

        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.toggleAnswerRevealAllowed(ToggleAnswerRevealAllowedCommand(quizId, participant)))
                .verifyComplete()

        verifyNoInteractions(eventBus)
    }

    @Test
    fun shouldCreateNewQuestionWithImage() {
        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()
        StepVerifier.create(quizService.createQuestion(CreateQuestionCommand(quizId, Question(questionId, "Wer ist das?", imageUrl = "pathToImage"))))
                .consumeNextWith {
                    verify(eventBus).post(argThat { (it as QuestionCreatedEvent).quizId == quizId && it.question == Question(questionId, "Wer ist das?", imageUrl = "pathToImage") })
                }
                .verifyComplete()
    }

    @Test
    fun shouldCreateNewQuestionWithCategory() {
        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()
        StepVerifier.create(quizService.createQuestion(CreateQuestionCommand(quizId, Question(questionId, "Wer ist das?", category = QuestionCategory("Geschichte")))))
                .consumeNextWith {
                    verify(eventBus).post(argThat { (it as QuestionCreatedEvent).quizId == quizId && it.question == Question(questionId, "Wer ist das?", category = QuestionCategory("Geschichte")) })
                }
                .verifyComplete()
    }

    @Test
    fun shouldAnswerQuestionCorrect() {
        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        val quizId = UUID.randomUUID()
        val participantId = UUID.randomUUID()
        StepVerifier.create(quizService.answer(AnswerCommand(quizId, participantId, AnswerCommand.Answer.CORRECT)))
                .consumeNextWith {
                    verify(eventBus).post(argThat { (it as AnsweredEvent).quizId == quizId && it.participantId == participantId && it.answer == AnswerCommand.Answer.CORRECT })
                }
                .verifyComplete()
    }

    @Test
    fun shouldAnswerQuestionIncorrect() {

        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        val quizId = UUID.randomUUID()
        val participantId = UUID.randomUUID()
        StepVerifier.create(quizService.answer(AnswerCommand(quizId, participantId, AnswerCommand.Answer.INCORRECT)))
                .consumeNextWith {
                    verify(eventBus).post(argThat { (it as AnsweredEvent).quizId == quizId && it.participantId == participantId && it.answer == AnswerCommand.Answer.INCORRECT })
                }
                .verifyComplete()
    }

    @Test
    fun shouldReopenQuestion() {
        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        val quizId = UUID.randomUUID()
        StepVerifier.create(quizService.reopenQuestion(ReopenCurrentQuestionCommand(quizId)))
                .consumeNextWith {
                    verify(eventBus).post(argThat { (it as CurrentQuestionReopenedEvent).quizId == quizId })
                }
                .verifyComplete()
    }

    @Test
    fun shouldReopenQuestionWithTimeConstaint() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()

        `when`(quizRepository.determineEvents(quizId))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Quiz")),
                        QuestionCreatedEvent(quizId, Question(questionId, "Warum ist die Banane krum?", initialTimeToAnswer = 3, secondsLeft = 3)),
                        QuestionAskedEvent(quizId, questionId),
                        CurrentQuestionReopenedEvent(quizId)
                ))

        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.reopenQuestion(ReopenCurrentQuestionCommand(quizId)))
                .consumeNextWith {
                    verify(eventBus).post(argThat { (it as CurrentQuestionReopenedEvent).quizId == quizId })
                }
                .verifyComplete()

        await ignoreException ClassCastException::class untilAsserted  {
            verify(eventBus, times(3)).post(argThat {
                it is TimeToAnswerDecreasedEvent
                        && it.quizId == quizId
                        && it.questionId == questionId
            })
        }
    }

    @Test
    fun shouldRevealAnswers() {
        val quizId = UUID.randomUUID()

        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.revealAnswers(RevealAnswersCommand(quizId)))
                .consumeNextWith {
                    verify(eventBus).post(argThat { (it as AnswersRevealedEvent).quizId == quizId })
                }
                .verifyComplete()
    }

    @Test
    fun shouldFinishQuiz() {
        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        val quizId = UUID.randomUUID()
        StepVerifier.create(quizService.finishQuiz(FinishQuizCommand(quizId)))
                .consumeNextWith {
                    verify(eventBus).post(argThat { (it as QuizFinishedEvent).quizId == quizId })
                }
                .verifyComplete()
    }

    @Test
    fun shouldDeleteQuiz() {
        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        val quizId = UUID.randomUUID()
        StepVerifier.create(quizService.deleteQuiz(DeleteQuizCommand(quizId)))
                .consumeNextWith {
                    verify(eventBus).post(argThat { (it as QuizDeletedEvent).quizId == quizId })
                }
                .verifyComplete()
    }

    @Test
    fun shouldDetermineQuizzes() {
        val quiz1Id = UUID.randomUUID()
        val quiz2Id = UUID.randomUUID()

        `when`(quizRepository.determineQuizIds())
                .thenReturn(Flux.just(quiz1Id, quiz2Id))

        `when`(quizRepository.determineEvents(quiz1Id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz1Id, Quiz(quiz1Id, "Quiz")),
                        QuestionCreatedEvent(quiz1Id, question = Question(question = "Warum ist die Banane krum?")),
                        ParticipantCreatedEvent(quiz1Id, Participant(name = "André")),
                        ParticipantCreatedEvent(quiz1Id, Participant(name = "Lena"))
                ))

        `when`(quizRepository.determineEvents(quiz2Id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz2Id, Quiz(quiz2Id, "Quiz")),
                        QuestionCreatedEvent(quiz2Id, question = Question(question = "Warum ist die Banane krum?")),
                        ParticipantCreatedEvent(quiz2Id, Participant(name = "André")),
                        ParticipantCreatedEvent(quiz2Id, Participant(name = "Lena"))
                ))

        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.determineQuizzes())
                .consumeNextWith { assertThat(it).hasId(quiz1Id) }
                .consumeNextWith { assertThat(it).hasId(quiz2Id) }
                .verifyComplete()
    }

    @Test
    fun shouldUndoAndRedoEvent() {
        val quizId = UUID.randomUUID()

        val lastEvent = QuizFinishedEvent(quizId)

        `when`(quizRepository.undoLastAction(quizId))
                .thenReturn(Mono.just(lastEvent))

        val quizService = DefaultQuizService(quizRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.undo(UndoCommand(quizId)))
                .consumeNextWith { verify(eventBus).post(ReloadQuizCommand(quizId)) }
                .verifyComplete()

        StepVerifier.create(quizService.redo(RedoCommand(quizId)))
                .consumeNextWith { verify(eventBus).post(lastEvent) }
                .verifyComplete()
    }

}
