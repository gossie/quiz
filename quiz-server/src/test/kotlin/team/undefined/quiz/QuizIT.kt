package team.undefined.quiz

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import team.undefined.quiz.web.QuestionDTO
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
                .expectBody().json("{\"name\":\"Quiz\"}")

        webTestClient
                .post()
                .uri("/api/quiz/1/participants")
                .contentType(MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue("André"))
                .exchange()
                .expectStatus().isCreated
                .expectBody().json("{\"quiz\":{\"name\":\"Quiz\",\"participants\":[{\"name\":\"André\",\"turn\":false,\"points\":0}]},\"participantId\":1}")

        webTestClient
                .post()
                .uri("/api/quiz/1/participants")
                .contentType(MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue("Lena"))
                .exchange()
                .expectStatus().isCreated
                .expectBody().json("{\"quiz\":{\"name\":\"Quiz\",\"participants\":[{\"name\":\"André\",\"turn\":false,\"points\":0},{\"name\":\"Lena\",\"turn\":false,\"points\":0}]},\"participantId\":2}")

        webTestClient
                .post()
                .uri("/api/quiz/1/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(QuestionDTO(question = "Wer schrieb das Buch Animal Farm?")))
                .exchange()
                .expectStatus().isCreated
                .expectBody().json("{\"name\":\"Quiz\",\"participants\":[{\"name\":\"André\",\"turn\":false,\"points\":0},{\"name\":\"Lena\",\"turn\":false,\"points\":0}],\"openQuestions\":[{\"question\":\"Wer schrieb das Buch Animal Farm?\"}]}")

        webTestClient
                .post()
                .uri("/api/quiz/1/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(QuestionDTO(question = "Wo befindet sich das Kahnbein?")))
                .exchange()
                .expectStatus().isCreated
                .expectBody().json("{\"name\":\"Quiz\",\"participants\":[{\"name\":\"André\",\"turn\":false,\"points\":0},{\"name\":\"Lena\",\"turn\":false,\"points\":0}],\"openQuestions\":[{\"question\":\"Wer schrieb das Buch Animal Farm?\"},{\"question\":\"Wo befindet sich das Kahnbein?\"}]}")

        webTestClient
                .put()
                .uri("/api/quiz/1/questions/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody().json("{\"name\":\"Quiz\",\"participants\":[{\"name\":\"André\",\"turn\":false,\"points\":0},{\"name\":\"Lena\",\"turn\":false,\"points\":0}],\"openQuestions\":[{\"question\":\"Wer schrieb das Buch Animal Farm?\",\"pending\":true},{\"question\":\"Wo befindet sich das Kahnbein?\"}]}")

        webTestClient
                .put()
                .uri("/api/quiz/1/participants/2/buzzer")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody().json("{\"name\":\"Quiz\",\"participants\":[{\"name\":\"André\",\"turn\":false,\"points\":0},{\"name\":\"Lena\",\"turn\":true,\"points\":0}],\"openQuestions\":[{\"question\":\"Wer schrieb das Buch Animal Farm?\",\"pending\":true},{\"question\":\"Wo befindet sich das Kahnbein?\"}]}")

        webTestClient
                .patch()
                .uri("/api/quiz/1")
                .contentType(MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue("true"))
                .exchange()
                .expectStatus().isOk
                .expectBody().json("{\"name\":\"Quiz\",\"participants\":[{\"name\":\"André\",\"turn\":false,\"points\":0},{\"name\":\"Lena\",\"turn\":true,\"points\":2}],\"playedQuestions\":[{\"question\":\"Wer schrieb das Buch Animal Farm?\",\"pending\":false}],\"openQuestions\":[{\"question\":\"Wo befindet sich das Kahnbein?\"}]}")

        webTestClient
                .put()
                .uri("/api/quiz/1/questions/2")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody().json("{\"name\":\"Quiz\",\"participants\":[{\"name\":\"André\",\"turn\":false,\"points\":0},{\"name\":\"Lena\",\"turn\":false,\"points\":2}],\"playedQuestions\":[{\"question\":\"Wer schrieb das Buch Animal Farm?\",\"pending\":false}],\"openQuestions\":[{\"question\":\"Wo befindet sich das Kahnbein?\",\"pending\":true}]}")

        webTestClient
                .put()
                .uri("/api/quiz/1/participants/1/buzzer")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody().json("{\"name\":\"Quiz\",\"participants\":[{\"name\":\"André\",\"turn\":true,\"points\":0},{\"name\":\"Lena\",\"turn\":false,\"points\":2}],\"playedQuestions\":[{\"question\":\"Wer schrieb das Buch Animal Farm?\",\"pending\":false}],\"openQuestions\":[{\"question\":\"Wo befindet sich das Kahnbein?\",\"pending\":true}]}")

        webTestClient
                .patch()
                .uri("/api/quiz/1")
                .contentType(MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue("false"))
                .exchange()
                .expectStatus().isOk
                .expectBody().json("{\"name\":\"Quiz\",\"participants\":[{\"name\":\"André\",\"turn\":true,\"points\":0},{\"name\":\"Lena\",\"turn\":false,\"points\":2}],\"playedQuestions\":[{\"question\":\"Wer schrieb das Buch Animal Farm?\",\"pending\":false}],\"openQuestions\":[{\"question\":\"Wo befindet sich das Kahnbein?\",\"pending\":true}]}")

        webTestClient
                .post()
                .uri("/api/quiz/1/participants")
                .contentType(MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue("Lena"))
                .exchange()
                .expectStatus().isCreated
                .expectBody().json("{\"quiz\":{\"name\":\"Quiz\",\"participants\":[{\"name\":\"André\",\"turn\":true,\"points\":0},{\"name\":\"Lena\",\"turn\":false,\"points\":2}],\"playedQuestions\":[{\"question\":\"Wer schrieb das Buch Animal Farm?\",\"pending\":false}],\"openQuestions\":[{\"question\":\"Wo befindet sich das Kahnbein?\",\"pending\":true}]},\"participantId\":2}")
    }
}
