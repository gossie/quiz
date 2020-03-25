package team.undefined.quiz.persistence

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.context.annotation.Import
import reactor.test.StepVerifier
import team.undefined.quiz.core.Quiz
import team.undefined.quiz.core.QuizService
import team.undefined.quiz.web.ReactiveWebSocketHandler

@DataR2dbcTest
@Import(DefaultQuizRepository::class, QuizService::class, ReactiveWebSocketHandler::class)
internal class DefaultQuizRepositoryTest {

    @Autowired
    private lateinit var defaultQuizRepository: DefaultQuizRepository;

    @Test
    fun shouldCreateAndDetermineChangeAndSaveQuiz() {
        StepVerifier.create(defaultQuizRepository.createQuiz(Quiz(name = "Quiz")))
                .expectNext(Quiz(1, "Quiz"))
                .verifyComplete();

        StepVerifier.create(defaultQuizRepository.determineQuiz(1))
                .expectNext(Quiz(1, "Quiz"))
                .verifyComplete();

        StepVerifier.create(defaultQuizRepository.saveQuiz(Quiz(1, "Quiz", listOf("Sandra", "Allli", "Erik"), "Erik")))
                .expectNext(Quiz(1, "Quiz", listOf("Sandra", "Allli", "Erik"), "Erik"))
                .verifyComplete();
    }

}