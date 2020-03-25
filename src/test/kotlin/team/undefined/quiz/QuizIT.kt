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
internal class QuizIT(@Autowired private val webTestClient: WebTestClient) {

    @Test
    fun shouldCreateAndGetQuiz() {
        webTestClient!!
                .post()
                .uri("/api/quiz")
                .body(BodyInserters.fromValue(QuizDTO("Q")))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated
                .expectBody().json("{\"name\":\"Q\"}")

        webTestClient
                .get()
                .uri("/api/quiz/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody().json("{\"name\":\"Q\"}")
    }
}