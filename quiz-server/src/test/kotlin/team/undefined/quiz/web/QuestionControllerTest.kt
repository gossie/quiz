package team.undefined.quiz.web

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.reactive.server.WebTestClient
import team.undefined.quiz.core.QuizService


@WebFluxTest(controllers = [QuestionController::class])
internal class QuestionControllerTest {

    @Autowired
    private lateinit var webClient: WebTestClient

    @MockBean
    private lateinit var quizService: QuizService

    @Test
    fun shouldReturnStatusConflictBecauseTheQuizIsFinished() {

    }

}