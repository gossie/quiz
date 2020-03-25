package team.undefined.quiz.web

import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Mono
import team.undefined.quiz.core.Quiz
import team.undefined.quiz.core.QuizService

@WebFluxTest(controllers = [QuizController::class])
@Import(ReactiveWebSocketHandler::class)
internal class QuizControllerTest {

    @Autowired private lateinit var webTestClient: WebTestClient
    @MockBean private lateinit var  quizService: QuizService

    @Test
    fun shouldCreateQuiz() {
        `when`(quizService.createQuiz(Quiz(name = "Q"))).thenReturn(Mono.just(Quiz(17, "Q")))
        `when`(quizService.determineQuiz(17)).thenReturn(Mono.just(Quiz(17, "Q")))

        webTestClient
                .post()
                .uri("/api/quiz")
                .body(BodyInserters.fromValue(QuizDTO("Q")))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated
                .expectBody().json("{\"name\":\"Q\"}")
    }

    @Test
    fun shouldGetQuiz() {
        `when`(quizService.determineQuiz(17)).thenReturn(Mono.just(Quiz(17, "Q")))

        webTestClient
                .get()
                .uri("/api/quiz/17")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody().json("{\"name\":\"Q\"}")
    }

}