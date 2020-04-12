package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.*

open class TestEventRepository : EventRepository {
    override fun storeEvent(event: Event): Mono<Event> {
        return Mono.just(event)
    }

    override fun determineEvents(quizId: UUID): Flux<Event> {
        return Flux.empty();
    }
}

internal class QuizServiceTest {

    private val PARTICIPANTS = listOf(Participant(UUID.randomUUID(), "Sandra"), Participant(UUID.randomUUID(), "Allli"), Participant(UUID.randomUUID(), "Erik"))

    private val quizRepository = spy(TestEventRepository())

    private val eventBus = mock(EventBus::class.java)

    @Test
    fun shouldCreateQuiz() {
        val quizService = QuizService(quizRepository, eventBus)
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

        val quizService = QuizService(quizRepository, eventBus)

        val participant = Participant(UUID.randomUUID(), "Lena")
        StepVerifier.create(quizService.createParticipant(CreateParticipantCommand(quizId, participant)))
                .consumeNextWith {
                    verify(eventBus).post(argThat { (it as ParticipantCreatedEvent).quizId == quizId && it.participant == participant })
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

        val quizService = QuizService(quizRepository, eventBus)

        val participant = Participant(name = "Sandra")
        StepVerifier.create(quizService.createParticipant(CreateParticipantCommand(quizId, participant)))
                .verifyComplete()

        verifyNoInteractions(eventBus);
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

        val quizService = QuizService(quizRepository, eventBus)

        val buzzer = quizService.buzzer(BuzzerCommand(quizId, lenasId))
        StepVerifier.create(buzzer)
                .consumeNextWith {
                    verify(eventBus).post(argThat { (it as BuzzeredEvent).quizId == quizId && it.participantId == lenasId })
                }
                .verifyComplete()
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

        val quizService = QuizService(quizRepository, eventBus)

        val buzzer = quizService.buzzer(BuzzerCommand(quizId, andresId))
        StepVerifier.create(buzzer)
                .verifyComplete()

        verifyNoInteractions(eventBus)
    }

    @Test
    fun shouldCreateQuestion() {
        val quizId = UUID.randomUUID()

        `when`(quizRepository.determineEvents(quizId))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Quiz"))
                ))

        val quizService = QuizService(quizRepository, eventBus)

        val questionId = UUID.randomUUID()
        StepVerifier.create(quizService.createQuestion(CreateQuestionCommand(quizId, Question(questionId, "Warum ist die Banane krum?"))))
                .consumeNextWith {
                    verify(eventBus).post(argThat { (it as QuestionCreatedEvent).quizId == quizId && it.question == Question(questionId, "Warum ist die Banane krum?") })
                }
                .verifyComplete()
    }

    @Test
    fun shouldStartNewQuestion() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()

        `when`(quizRepository.determineEvents(quizId))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Quiz")),
                        QuestionCreatedEvent(quizId, Question(questionId, "Warum ist die Banane krum?"))
                ))

        val quizService = QuizService(quizRepository, eventBus)

        StepVerifier.create(quizService.startNewQuestion(AskQuestionCommand(quizId, questionId)))
                .consumeNextWith {
                    verify(eventBus).post(argThat { (it as QuestionAskedEvent).quizId == quizId && it.questionId == questionId })
                }
                .verifyComplete()
    }
/*
    @Test
    fun shouldStopPendingOldQuestionWhenNewQuestionIsStarted() {
        val quizRepository = mock(EventRepository::class.java)
        `when`(quizRepository.determineQuiz(117))
                .thenReturn(Mono.just(Quiz(117, "Quiz", listOf(Participant(23, "Sandra", true), Participant(24, "Allli"), Participant(25, "Erik")), mutableListOf(Question(12, "Warum ist die Banane krumm?", pending = true), Question(13, "Wie hoch ist die Zugspitze?")))))
        `when`(quizRepository.saveQuiz(Quiz(117, "Quiz", PARTICIPANTS, listOf(Question(12, "Warum ist die Banane krumm?", false, alreadyPlayed = true), Question(13, "Wie hoch ist die Zugspitze?", pending = true)))))
                .thenReturn(Mono.just(Quiz(117, "Quiz", PARTICIPANTS, listOf(Question(12, "Warum ist die Banane krumm?", false, alreadyPlayed = true), Question(13, "Wie hoch ist die Zugspitze?", true)))))

        val quizService = QuizService(quizRepository)

        val observedQuiz = AtomicReference<Quiz>()
        quizService.observeQuiz(117)
                .subscribe { observedQuiz.set(it) }

        StepVerifier.create(quizService.startNewQuestion(117, 13))
                .expectNext(Quiz(117, "Quiz", PARTICIPANTS, listOf(Question(12, "Warum ist die Banane krumm?", false, alreadyPlayed = true), Question(13, "Wie hoch ist die Zugspitze?", true))))
                .verifyComplete()

        assertThat(observedQuiz.get()).isEqualTo(Quiz(117, "Quiz", PARTICIPANTS, listOf(Question(12, "Warum ist die Banane krumm?", false, alreadyPlayed = true), Question(13, "Wie hoch ist die Zugspitze?", true))))
    }
*/
    @Test
    fun shouldCreateNewQuestionWithImage() {
        val quizService = QuizService(quizRepository, eventBus)

        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()
        StepVerifier.create(quizService.createQuestion(CreateQuestionCommand(quizId, Question(questionId, "Wer ist das?", imageUrl = "pathToImage"))))
                .consumeNextWith {
                    verify(eventBus).post(argThat { (it as QuestionCreatedEvent).quizId == quizId && it.question == Question(questionId, "Wer ist das?", imageUrl = "pathToImage") })
                }
                .verifyComplete()
    }

    @Test
    fun shouldAnswerQuestionCorrect() {
        val quizService = QuizService(quizRepository, eventBus)

        val quizId = UUID.randomUUID()
        StepVerifier.create(quizService.answer(AnswerCommand(quizId, AnswerCommand.Answer.CORRECT)))
                .consumeNextWith {
                    verify(eventBus).post(argThat { (it as AnsweredEvent).quizId == quizId && it.answer == AnswerCommand.Answer.CORRECT })
                }
                .verifyComplete()
    }

    @Test
    fun shouldAnswerQuestionIncorrect() {

        val quizService = QuizService(quizRepository, eventBus)

        val quizId = UUID.randomUUID()
        StepVerifier.create(quizService.answer(AnswerCommand(quizId, AnswerCommand.Answer.INCORRECT)))
                .consumeNextWith {
                    verify(eventBus).post(argThat { (it as AnsweredEvent).quizId == quizId && it.answer == AnswerCommand.Answer.INCORRECT })
                }
                .verifyComplete()
    }
/*
    @Test
    fun shouldNotGetNegativePointsAfterIncorrectAnswer() {
        val quizRepository = mock(EventRepository::class.java)
        `when`(quizRepository.determineQuiz(117))
                .thenReturn(Mono.just(Quiz(117, "Quiz", listOf(Participant(23, "Sandra", true), Participant(24, "Allli"), Participant(25, "Erik")), listOf(Question(12, "Warum ist die Banane krumm?", true)))))
        `when`(quizRepository.saveQuiz(Quiz(117, "Quiz", listOf(Participant(23, "Sandra", true), Participant(24, "Allli"), Participant(25, "Erik")), listOf(Question(12, "Warum ist die Banane krumm?", true)))))
                .thenReturn(Mono.just(Quiz(117, "Quiz", listOf(Participant(23, "Sandra", true), Participant(24, "Allli"), Participant(25, "Erik")), listOf(Question(12, "Warum ist die Banane krumm?", true)))))

        val quizService = QuizService(quizRepository)

        val observedQuiz = AtomicReference<Quiz>()
        quizService.observeQuiz(117)
                .subscribe { observedQuiz.set(it) }

        StepVerifier.create(quizService.incorrectAnswer(117))
                .expectNext(Quiz(117, "Quiz", listOf(Participant(23, "Sandra", true), Participant(24, "Allli"), Participant(25, "Erik")), listOf(Question(12, "Warum ist die Banane krumm?", true))))
                .verifyComplete()

        assertThat(observedQuiz.get()).isEqualTo(Quiz(117, "Quiz", listOf(Participant(23, "Sandra", true), Participant(24, "Allli"), Participant(25, "Erik")), listOf(Question(12, "Warum ist die Banane krumm?", true))))
    }
*/
    @Test
    fun shouldReopenQuestion() {
        val quizService = QuizService(quizRepository, eventBus)

        val quizId = UUID.randomUUID()
        StepVerifier.create(quizService.reopenQuestion(ReopenCurrentQuestionCommand(quizId)))
                .consumeNextWith {
                    verify(eventBus).post(argThat { (it as CurrentQuestionReopenedEvent).quizId == quizId })
                }
                .verifyComplete()
    }

}