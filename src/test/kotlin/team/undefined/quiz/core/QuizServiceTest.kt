package team.undefined.quiz.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

internal class QuizServiceTest {

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
        `when`(quizRepository.saveQuiz(Quiz(12, "Quiz", listOf("Sandra")))).thenReturn(Mono.just(Quiz(12, "Quiz", listOf("Sandra"))))

        val quizService = QuizService(quizRepository)

        StepVerifier.create(quizService.createParticipant(12, "Sandra"))
                .expectNext(Quiz(12, "Quiz", listOf("Sandra")))
                .verifyComplete()
    }

    @Test
    fun shouldBuzzer() {
        val quizRepository = mock(QuizRepository::class.java)
        `when`(quizRepository.determineQuiz(7))
                .thenReturn(Mono.just(Quiz(7, "Quiz", listOf("Sandra", "Allli", "Erik"))))
        `when`(quizRepository.saveQuiz(Quiz(7, "Quiz", listOf("Sandra", "Allli", "Erik"), "Sandra")))
                .thenReturn(Mono.just(Quiz(7, "Quiz", listOf("Sandra", "Allli", "Erik"), "Sandra")))

        val quizService = QuizService(quizRepository)

        val  sb = StringBuilder()
        quizService.observeBuzzer()
                .subscribe { sb.append(it) }

        val buzzer = quizService.buzzer(7, "Sandra")
        StepVerifier.create(buzzer)
                .expectNext(Quiz(7, "Quiz", listOf("Sandra", "Allli", "Erik"), "Sandra"))
                .verifyComplete()

        assertThat(sb.toString()).isEqualTo("Sandra");
    }

}