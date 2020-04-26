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
import team.undefined.quiz.core.*
import java.util.*

@WebFluxTest(controllers = [QuestionController::class])
class QuestionControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient
    @MockBean
    private lateinit var  quizService: QuizService
    @MockBean
    private lateinit var quizProjection: QuizProjection

    @Test
    fun shouldCreateQuestion() {
        val quizId = UUID.randomUUID()

        `when`(quizService.createQuestion(CreateQuestionCommand(quizId, Question(question = "Wofür steht die Abkürzung a.d.?"))))
                .thenReturn(Mono.just(Unit))

        webTestClient
                .post()
                .uri("/api/quiz/$quizId/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(QuestionDTO(question = "Wofür steht die Abkürzung a.d.?")))
                .exchange()
                .expectStatus().isCreated
    }

    @Test
    fun shouldCreateQuestionWithImage() {
        val quizId = UUID.randomUUID()

        `when`(quizService.createQuestion(CreateQuestionCommand(quizId, Question(question = "Wer ist das", imageUrl = "pathToImage"))))
                .thenReturn(Mono.just(Unit))

        webTestClient
                .post()
                .uri("/api/quiz/$quizId/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(QuestionDTO(question = "Wer ist das?", imagePath = "pathToImage")))
                .exchange()
                .expectStatus().isCreated
    }

    @Test
    fun shouldStartQuestion() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()

        `when`(quizService.startNewQuestion(AskQuestionCommand(quizId, questionId)))
                .thenReturn(Mono.just(Unit))

        webTestClient
                .put()
                .uri("/api/quiz/$quizId/questions/$questionId")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk

    }
}