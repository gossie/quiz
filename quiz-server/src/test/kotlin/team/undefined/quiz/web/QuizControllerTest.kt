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

@WebFluxTest(controllers = [QuizController::class])
@Import(ReactiveWebSocketHandler::class)
internal class QuizControllerTest {

    @Autowired private lateinit var webTestClient: WebTestClient
    @MockBean private lateinit var  quizService: QuizService
    @MockBean private lateinit var quizProjection: QuizProjection

    @Test
    fun shouldCreateQuiz() {
        val quiz = Quiz(name = "Awesome Quiz")
        `when`(quizService.createQuiz(CreateQuizCommand(quiz.id, quiz)))
                .thenReturn(Mono.just(Unit))

        webTestClient
                .post()
                .uri("/api/quiz")
                .body(BodyInserters.fromValue(QuizDTO(name = "Awesome Quiz")))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated
    }

    @Test
    fun shouldAnswerCorrect() {
        val quizId = UUID.randomUUID()

        `when`(quizService.answer(AnswerCommand(quizId, AnswerCommand.Answer.CORRECT)))
                .thenReturn(Mono.just(Unit))

        webTestClient
                .patch()
                .uri("/api/quiz/$quizId")
                .contentType(MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue("true"))
                .exchange()
                .expectStatus().isOk
    }

    @Test
    fun shouldAnswerIncorrect() {
        val quizId = UUID.randomUUID()

        `when`(quizService.answer(AnswerCommand(quizId, AnswerCommand.Answer.INCORRECT)))
                .thenReturn(Mono.just(Unit))

        webTestClient
                .patch()
                .uri("/api/quiz/$quizId")
                .contentType(MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue("false"))
                .exchange()
                .expectStatus().isOk
    }

    @Test
    fun shouldRepopenQuestion() {
        val quizId = UUID.randomUUID()

        `when`(quizService.reopenQuestion(ReopenCurrentQuestionCommand(quizId)))
                .thenReturn(Mono.just(Unit))

        webTestClient
                .put()
                .uri("/api/quiz/$quizId")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
    }

}