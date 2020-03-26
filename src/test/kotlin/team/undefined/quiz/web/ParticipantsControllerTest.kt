package team.undefined.quiz.web

import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Mono
import team.undefined.quiz.core.Quiz
import team.undefined.quiz.core.QuizService

@WebFluxTest(controllers = [ParticipantsController::class])
@Import(ReactiveWebSocketHandler::class)
internal class ParticipantsControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient
    @MockBean
    private lateinit var  quizService: QuizService

    @Test
    fun shouldCreateParticipant() {
        `when`(quizService.createParticipant(7, "Erik"))
                .thenReturn(Mono.just(Quiz(7, "Quiz", listOf("Erik"))))

        webTestClient
                .post()
                .uri("/api/quiz/7/participants")
                .contentType(MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue("Erik"))
                .exchange()
                .expectStatus().isCreated
                .expectBody().json("{\"id\":7,\"name\":\"Quiz\",\"participants\":[\"Erik\"],\"links\":[]}")
    }

    @Test
    fun shouldBuzzer() {
        `when`(quizService.buzzer(17, "Sandra"))
                .thenReturn(Mono.just(Quiz(17, "Quiz", listOf("Erik", "Allli", "Sandra"), "Sandra")))

        webTestClient
                .put()
                .uri("/api/quiz/17/participants/Sandra/buzzer")
                .exchange()
                .expectStatus().isOk
    }
}