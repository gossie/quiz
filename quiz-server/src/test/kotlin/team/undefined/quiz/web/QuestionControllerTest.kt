package team.undefined.quiz.web

import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Mono
import team.undefined.quiz.core.Participant
import team.undefined.quiz.core.Question
import team.undefined.quiz.core.Quiz
import team.undefined.quiz.core.QuizService

@WebFluxTest(controllers = [QuestionController::class])
@Import(ReactiveWebSocketHandler::class)
class QuestionControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient
    @MockBean
    private lateinit var  quizService: QuizService

    @Test
    fun shouldstartNewQuestion() {
        `when`(quizService.startNewQuestion(11,"Wofür steht die Abkürzung a.d.?"))
                .thenReturn(Mono.just(Quiz(11, "Quiz", emptyList(), listOf(Question(question = "Wofür steht die Abkürzung a.d.?")))))

        webTestClient
                .post()
                .uri("/api/quiz/11/questions")
                .contentType(MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue("Wofür steht die Abkürzung a.d.?"))
                .exchange()
                .expectStatus().isCreated
                .expectBody().json("{\"id\":11,\"name\":\"Quiz\",\"questions\":[\"Wofür steht die Abkürzung a.d.?\"],\"links\":[{\"href\":\"/api/quiz/11/participants\",\"rel\":\"createParticipant\"},{\"href\":\"/api/quiz/11/questions\",\"rel\":\"createQuestion\"}]}")
    }
}