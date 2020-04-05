package team.undefined.quiz.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.concurrent.atomic.AtomicReference

internal class QuizServiceTest {

    private val PARTICIPANTS = listOf(Participant(23, "Sandra"), Participant(24, "Allli"), Participant(25, "Erik"))

    @Test
    fun shouldCreateQuiz() {
        val quizRepository = mock(QuizRepository::class.java)
        `when`(quizRepository.createQuiz(Quiz(name = "Quiz"))).thenReturn(Mono.just(Quiz(12, "Quiz")))

        val quizService = QuizService(quizRepository)
        StepVerifier.create(quizService.createQuiz(Quiz(name = "Quiz")))
                .expectNext(Quiz(12, "Quiz"))
                .verifyComplete()
    }

    @Test
    fun shouldDetermineQuiz() {
        val quizRepository = mock(QuizRepository::class.java)
        `when`(quizRepository.determineQuiz(12)).thenReturn(Mono.just(Quiz(12, "Quiz")))

        val quizService = QuizService(quizRepository)

        StepVerifier.create(quizService.determineQuiz(12))
                .expectNext(Quiz(12, "Quiz"))
                .verifyComplete()
    }

    @Test
    fun shouldCreateParticipant() {
        val quizRepository = mock(QuizRepository::class.java)
        `when`(quizRepository.determineQuiz(12)).thenReturn(Mono.just(Quiz(12, "Quiz")))
        `when`(quizRepository.saveQuiz(Quiz(12, "Quiz", listOf(Participant(name = "Sandra"))))).thenReturn(Mono.just(Quiz(12, "Quiz", listOf(Participant(23, "Sandra")))))

        val quizService = QuizService(quizRepository)

        val observedQuiz = AtomicReference<Quiz>()
        quizService.observeQuiz(12)
                .subscribe { observedQuiz.set(it) }

        StepVerifier.create(quizService.createParticipant(12, "Sandra"))
                .expectNext(Quiz(12, "Quiz", listOf(Participant(23, "Sandra"))))
                .verifyComplete()

        assertThat(observedQuiz.get()).isEqualTo(Quiz(12, "Quiz", listOf(Participant(23, "Sandra"))))
    }

    @Test
    fun shouldNotCreateParticipantWithTheSameName() {
        val quizRepository = mock(QuizRepository::class.java)
        `when`(quizRepository.determineQuiz(12)).thenReturn(Mono.just(Quiz(12, "Quiz", listOf(Participant(23, "Sandra")))))
        `when`(quizRepository.saveQuiz(Quiz(12, "Quiz", listOf(Participant(23, "Sandra"))))).thenReturn(Mono.just(Quiz(12, "Quiz", listOf(Participant(23, "Sandra")))))

        val quizService = QuizService(quizRepository)

        val observedQuiz = AtomicReference<Quiz>()
        quizService.observeQuiz(12)
                .subscribe { observedQuiz.set(it) }

        StepVerifier.create(quizService.createParticipant(12, "Sandra"))
                .expectNext(Quiz(12, "Quiz", listOf(Participant(23, "Sandra"))))
                .verifyComplete()

        assertThat(observedQuiz.get()).isEqualTo(Quiz(12, "Quiz", listOf(Participant(23, "Sandra"))))
    }

    @Test
    fun shouldBuzzer() {
        val quizRepository = mock(QuizRepository::class.java)
        `when`(quizRepository.determineQuiz(7))
                .thenReturn(Mono.just(Quiz(7, "Quiz", PARTICIPANTS)))
        `when`(quizRepository.saveQuiz(Quiz(7, "Quiz", listOf(Participant(23, "Sandra", true), Participant(24, "Allli"), Participant(25, "Erik")), emptyList())))
                .thenReturn(Mono.just(Quiz(7, "Quiz", listOf(Participant(23, "Sandra", true), Participant(24, "Allli"), Participant(25, "Erik")), emptyList())))

        val quizService = QuizService(quizRepository)

        val observedQuiz = AtomicReference<Quiz>()
        quizService.observeQuiz(7)
                .subscribe { observedQuiz.set(it) }

        val buzzer = quizService.buzzer(7, 23)
        StepVerifier.create(buzzer)
                .expectNext(Quiz(7, "Quiz", listOf(Participant(23, "Sandra", true), Participant(24, "Allli"), Participant(25, "Erik")), emptyList()))
                .verifyComplete()

        assertThat(observedQuiz.get()).isEqualTo(Quiz(7, "Quiz", listOf(Participant(23, "Sandra", true), Participant(24, "Allli"), Participant(25, "Erik")), emptyList()))
    }

    @Test
    fun shouldNotAllowSecondBuzzer() {
        val quizRepository = mock(QuizRepository::class.java)
        `when`(quizRepository.determineQuiz(7))
                .thenReturn(Mono.just(Quiz(7, "Quiz", PARTICIPANTS)))
                .thenReturn(Mono.just(Quiz(7, "Quiz", listOf(Participant(23, "Sandra", true), Participant(24, "Allli"), Participant(25, "Erik")), emptyList())))
        `when`(quizRepository.saveQuiz(Quiz(7, "Quiz", listOf(Participant(23, "Sandra", true), Participant(24, "Allli"), Participant(25, "Erik")), emptyList())))
                .thenReturn(Mono.just(Quiz(7, "Quiz", listOf(Participant(23, "Sandra", true), Participant(24, "Allli"), Participant(25, "Erik")), emptyList())))

        val quizService = QuizService(quizRepository)

        val observedQuiz = AtomicReference<Quiz>()
        quizService.observeQuiz(7)
                .subscribe { observedQuiz.set(it) }

        StepVerifier.create(quizService.buzzer(7, 23))
                .expectNext(Quiz(7, "Quiz", listOf(Participant(23, "Sandra", true), Participant(24, "Allli"), Participant(25, "Erik")), emptyList()))
                .verifyComplete()

        assertThat(observedQuiz.get()).isEqualTo(Quiz(7, "Quiz", listOf(Participant(23, "Sandra", true), Participant(24, "Allli"), Participant(25, "Erik")), emptyList()))

        val buzzer = quizService.buzzer(7, 24)
        StepVerifier.create(buzzer)
                .verifyComplete()

        assertThat(observedQuiz.get()).isEqualTo(Quiz(7, "Quiz", listOf(Participant(23, "Sandra", true), Participant(24, "Allli"), Participant(25, "Erik")), emptyList()))
    }

    @Test
    fun shouldStartNewQuestion() {
        val quizRepository = mock(QuizRepository::class.java)
        `when`(quizRepository.determineQuiz(117))
                .thenReturn(Mono.just(Quiz(117, "Quiz", listOf(Participant(23, "Sandra", true), Participant(24, "Allli"), Participant(25, "Erik")))))
        `when`(quizRepository.saveQuiz(Quiz(117, "Quiz", PARTICIPANTS, listOf(Question(question = "Warum ist die Banane krumm?", pending = true)))))
                .thenReturn(Mono.just(Quiz(117, "Quiz", PARTICIPANTS, listOf(Question(12, "Warum ist die Banane krumm?",true)))))

        val quizService = QuizService(quizRepository)

        val observedQuiz = AtomicReference<Quiz>()
        quizService.observeQuiz(117)
                .subscribe { observedQuiz.set(it) }

        StepVerifier.create(quizService.startNewQuestion(117, "Warum ist die Banane krumm?"))
                .expectNext(Quiz(117, "Quiz", PARTICIPANTS, listOf(Question(12, "Warum ist die Banane krumm?",true))))
                .verifyComplete()

        assertThat(observedQuiz.get()).isEqualTo(Quiz(117, "Quiz", PARTICIPANTS, listOf(Question(12, "Warum ist die Banane krumm?",true))))
    }

    @Test
    fun shouldStopPendingOldQuestionWhenNewQuestionIsStarted() {
        val quizRepository = mock(QuizRepository::class.java)
        `when`(quizRepository.determineQuiz(117))
                .thenReturn(Mono.just(Quiz(117, "Quiz", listOf(Participant(23, "Sandra", true), Participant(24, "Allli"), Participant(25, "Erik")), mutableListOf(Question(12, "Warum ist die Banane krumm?", pending = true)))))
        `when`(quizRepository.saveQuiz(Quiz(117, "Quiz", PARTICIPANTS, listOf(Question(12, "Warum ist die Banane krumm?", false), Question(question = "Wie hoch ist die Zugspitze?", pending = true)))))
                .thenReturn(Mono.just(Quiz(117, "Quiz", PARTICIPANTS, listOf(Question(12, "Warum ist die Banane krumm?",false), Question(13, "Wie hoch ist die Zugspitze?", true)))))

        val quizService = QuizService(quizRepository)

        val observedQuiz = AtomicReference<Quiz>()
        quizService.observeQuiz(117)
                .subscribe { observedQuiz.set(it) }

        StepVerifier.create(quizService.startNewQuestion(117, "Wie hoch ist die Zugspitze?"))
                .expectNext(Quiz(117, "Quiz", PARTICIPANTS, listOf(Question(12, "Warum ist die Banane krumm?",false), Question(13, "Wie hoch ist die Zugspitze?", true))))
                .verifyComplete()

        assertThat(observedQuiz.get()).isEqualTo(Quiz(117, "Quiz", PARTICIPANTS, listOf(Question(12, "Warum ist die Banane krumm?",false), Question(13, "Wie hoch ist die Zugspitze?", true))))
    }

    @Test
    fun shouldStartNewQuestionWithImage() {
        val quizRepository = mock(QuizRepository::class.java)
        `when`(quizRepository.determineQuiz(117))
                .thenReturn(Mono.just(Quiz(117, "Quiz", listOf(Participant(23, "Sandra", true), Participant(24, "Allli"), Participant(25, "Erik")))))
        `when`(quizRepository.saveQuiz(Quiz(117, "Quiz", PARTICIPANTS, listOf(Question(question = "Wer ist das?", pending = true, imagePath = "pathToImage")))))
                .thenReturn(Mono.just(Quiz(117, "Quiz", PARTICIPANTS, listOf(Question(12, "Wer ist das?",true, "pathToImage")))))

        val quizService = QuizService(quizRepository)

        val observedQuiz = AtomicReference<Quiz>()
        quizService.observeQuiz(117)
                .subscribe { observedQuiz.set(it) }

        StepVerifier.create(quizService.startNewQuestion(117, "Wer ist das?", "pathToImage"))
                .expectNext(Quiz(117, "Quiz", PARTICIPANTS, listOf(Question(12, "Wer ist das?",true, "pathToImage"))))
                .verifyComplete()

        assertThat(observedQuiz.get()).isEqualTo(Quiz(117, "Quiz", PARTICIPANTS, listOf(Question(12, "Wer ist das?",true, "pathToImage"))))
    }

    @Test
    fun shouldAnswerQuestionCorrect() {
        val quizRepository = mock(QuizRepository::class.java)
        `when`(quizRepository.determineQuiz(117))
                .thenReturn(Mono.just(Quiz(117, "Quiz", listOf(Participant(23, "Sandra", true), Participant(24, "Allli"), Participant(25, "Erik")), listOf(Question(12, "Warum ist die Banane krumm?", true)))))
        `when`(quizRepository.saveQuiz(Quiz(117, "Quiz", listOf(Participant(23, "Sandra", true, 2), Participant(24, "Allli"), Participant(25, "Erik")), listOf(Question(12, "Warum ist die Banane krumm?")))))
                .thenReturn(Mono.just(Quiz(117, "Quiz", listOf(Participant(23, "Sandra", true, 2), Participant(24, "Allli"), Participant(25, "Erik")), listOf(Question(12, "Warum ist die Banane krumm?")))))

        val quizService = QuizService(quizRepository)

        val observedQuiz = AtomicReference<Quiz>()
        quizService.observeQuiz(117)
                .subscribe { observedQuiz.set(it) }

        StepVerifier.create(quizService.correctAnswer(117))
                .expectNext(Quiz(117, "Quiz", listOf(Participant(23, "Sandra", true, 2), Participant(24, "Allli"), Participant(25, "Erik")), listOf(Question(12, "Warum ist die Banane krumm?"))))
                .verifyComplete()

        assertThat(observedQuiz.get()).isEqualTo(Quiz(117, "Quiz", listOf(Participant(23, "Sandra", true, 2), Participant(24, "Allli"), Participant(25, "Erik")), listOf(Question(12, "Warum ist die Banane krumm?"))))
    }

    @Test
    fun shouldAnswerQuestionIncorrect() {
        val quizRepository = mock(QuizRepository::class.java)
        `when`(quizRepository.determineQuiz(117))
                .thenReturn(Mono.just(Quiz(117, "Quiz", listOf(Participant(23, "Sandra", true, 2), Participant(24, "Allli"), Participant(25, "Erik")), listOf(Question(12, "Warum ist die Banane krumm?", true)))))
        `when`(quizRepository.saveQuiz(Quiz(117, "Quiz", listOf(Participant(23, "Sandra", true, 1), Participant(24, "Allli"), Participant(25, "Erik")), listOf(Question(12, "Warum ist die Banane krumm?", true)))))
                .thenReturn(Mono.just(Quiz(117, "Quiz", listOf(Participant(23, "Sandra", true, 1), Participant(24, "Allli"), Participant(25, "Erik")), listOf(Question(12, "Warum ist die Banane krumm?", true)))))

        val quizService = QuizService(quizRepository)

        val observedQuiz = AtomicReference<Quiz>()
        quizService.observeQuiz(117)
                .subscribe { observedQuiz.set(it) }

        StepVerifier.create(quizService.incorrectAnswer(117))
                .expectNext(Quiz(117, "Quiz", listOf(Participant(23, "Sandra", true, 1), Participant(24, "Allli"), Participant(25, "Erik")), listOf(Question(12, "Warum ist die Banane krumm?", true))))
                .verifyComplete()

        assertThat(observedQuiz.get()).isEqualTo(Quiz(117, "Quiz", listOf(Participant(23, "Sandra", true, 1), Participant(24, "Allli"), Participant(25, "Erik")), listOf(Question(12, "Warum ist die Banane krumm?", true))))
    }

    @Test
    fun shouldNotGetNegativePointsAfterIncorrectAnswer() {
        val quizRepository = mock(QuizRepository::class.java)
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

    @Test
    fun shouldReopenQuestion() {
        val quizRepository = mock(QuizRepository::class.java)
        `when`(quizRepository.determineQuiz(117))
                .thenReturn(Mono.just(Quiz(117, "Quiz", listOf(Participant(23, "Sandra", true), Participant(24, "Allli"), Participant(25, "Erik")), listOf(Question(12, "Warum ist die Banane krumm?", true)))))
        `when`(quizRepository.saveQuiz(Quiz(117, "Quiz", listOf(Participant(23, "Sandra", false), Participant(24, "Allli"), Participant(25, "Erik")), listOf(Question(12, "Warum ist die Banane krumm?", true)))))
                .thenReturn(Mono.just(Quiz(117, "Quiz", listOf(Participant(23, "Sandra", false), Participant(24, "Allli"), Participant(25, "Erik")), listOf(Question(12, "Warum ist die Banane krumm?", true)))))

        val quizService = QuizService(quizRepository)

        val observedQuiz = AtomicReference<Quiz>()
        quizService.observeQuiz(117)
                .subscribe { observedQuiz.set(it) }

        StepVerifier.create(quizService.reopenQuestion(117))
                .expectNext(Quiz(117, "Quiz", listOf(Participant(23, "Sandra", false), Participant(24, "Allli"), Participant(25, "Erik")), listOf(Question(12, "Warum ist die Banane krumm?", true))))
                .verifyComplete()

        assertThat(observedQuiz.get()).isEqualTo(Quiz(117, "Quiz", listOf(Participant(23, "Sandra", false), Participant(24, "Allli"), Participant(25, "Erik")), listOf(Question(12, "Warum ist die Banane krumm?", true))))
    }
}