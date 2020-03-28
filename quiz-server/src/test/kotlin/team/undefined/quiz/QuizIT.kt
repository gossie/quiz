package team.undefined.quiz

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import team.undefined.quiz.web.QuizDTO

@SpringBootTest
@AutoConfigureWebTestClient
internal class QuizIT() {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun shouldCreateAndGetQuiz() {
        webTestClient
                .post()
                .uri("/api/quiz")
                .body(BodyInserters.fromValue(QuizDTO(name = "Quiz")))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated
                .expectBody().json("{\"name\":\"Quiz\",\"links\":[{\"href\":\"/api/quiz/1/participants\",\"rel\":\"createParticipant\"},{\"href\":\"/api/quiz/1/questions\",\"rel\":\"createQuestion\"}]}")

        webTestClient
                .post()
                .uri("/api/quiz/1/participants")
                .contentType(MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue("André"))
                .exchange()
                .expectStatus().isCreated
                .expectBody().json("{\"name\":\"Quiz\",\"participants\":[\"André\"],\"links\":[{\"href\":\"/api/quiz/1/participants\",\"rel\":\"createParticipant\"},{\"href\":\"/api/quiz/1/questions\",\"rel\":\"createQuestion\"}]}")

        webTestClient
                .post()
                .uri("/api/quiz/1/participants")
                .contentType(MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue("Lena"))
                .exchange()
                .expectStatus().isCreated
                .expectBody().json("{\"name\":\"Quiz\",\"participants\":[\"André\",\"Lena\"],\"links\":[{\"href\":\"/api/quiz/1/participants\",\"rel\":\"createParticipant\"},{\"href\":\"/api/quiz/1/questions\",\"rel\":\"createQuestion\"}]}")
    }
}