package team.undefined.quiz.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.concurrent.atomic.AtomicReference

internal class QuizServiceTest {

    private val PARTICIPANTS = listOf(Participant(23, "Sandra"), Participant(23, "Allli"), Participant(23, "Erik"))

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

        val observedQuiz = AtomicReference<Quiz>();
        quizService.observeQuiz()
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
        `when`(quizRepository.saveQuiz(Quiz(7, "Quiz", PARTICIPANTS, emptyList(), "Sandra")))
                .thenReturn(Mono.just(Quiz(7, "Quiz", PARTICIPANTS, emptyList(), "Sandra")))

        val quizService = QuizService(quizRepository)

        val observedQuiz = AtomicReference<Quiz>();
        quizService.observeQuiz()
                .subscribe { observedQuiz.set(it) }

        val buzzer = quizService.buzzer(7, "Sandra")
        StepVerifier.create(buzzer)
                .expectNext(Quiz(7, "Quiz", PARTICIPANTS, emptyList(), "Sandra"))
                .verifyComplete()

        assertThat(observedQuiz.get()).isEqualTo(Quiz(7, "Quiz", PARTICIPANTS, emptyList(), "Sandra"))
    }

    @Test
    fun shouldNotAllowSecondBuzzer() {
        val quizRepository = mock(QuizRepository::class.java)
        `when`(quizRepository.determineQuiz(7))
                .thenReturn(Mono.just(Quiz(7, "Quiz", PARTICIPANTS)))
                .thenReturn(Mono.just(Quiz(7, "Quiz", PARTICIPANTS, emptyList(), "Sandra")))
        `when`(quizRepository.saveQuiz(Quiz(7, "Quiz", PARTICIPANTS, emptyList(), "Sandra")))
                .thenReturn(Mono.just(Quiz(7, "Quiz", PARTICIPANTS, emptyList(), "Sandra")))

        val quizService = QuizService(quizRepository)

        val observedQuiz = AtomicReference<Quiz>();
        quizService.observeQuiz()
                .subscribe { observedQuiz.set(it) }

        StepVerifier.create(quizService.buzzer(7, "Sandra"))
                .expectNext(Quiz(7, "Quiz", PARTICIPANTS, emptyList(), "Sandra"))
                .verifyComplete()

        assertThat(observedQuiz.get()).isEqualTo(Quiz(7, "Quiz", PARTICIPANTS, emptyList(), "Sandra"));

        val buzzer = quizService.buzzer(7, "Allli")
        StepVerifier.create(buzzer)
                .verifyComplete()

        assertThat(observedQuiz.get()).isEqualTo(Quiz(7, "Quiz", PARTICIPANTS, emptyList(), "Sandra"))
    }

    @Test
    fun shouldStartNewQuestion() {
        val quizRepository = mock(QuizRepository::class.java)
        `when`(quizRepository.determineQuiz(117))
                .thenReturn(Mono.just(Quiz(117, "Quiz", PARTICIPANTS, emptyList(), "Sandra")))
        `when`(quizRepository.saveQuiz(Quiz(117, "Quiz", PARTICIPANTS)))
                .thenReturn(Mono.just(Quiz(117, "Quiz", PARTICIPANTS)))

        val quizService = QuizService(quizRepository)

        val observedQuiz = AtomicReference<Quiz>();
        quizService.observeQuiz()
                .subscribe { observedQuiz.set(it) }

        StepVerifier.create(quizService.startNewQuestion(117))
                .expectNext(Quiz(117, "Quiz", PARTICIPANTS))
                .verifyComplete()

        assertThat(Quiz(117, "Quiz", PARTICIPANTS))
    }

}