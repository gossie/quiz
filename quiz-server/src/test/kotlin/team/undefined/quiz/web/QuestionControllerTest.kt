package team.undefined.quiz.web

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Mono
import team.undefined.quiz.core.*
import java.util.*


@WebFluxTest(controllers = [QuestionController::class])
internal class QuestionControllerTest {

    @Autowired
    private lateinit var webClient: WebTestClient

    @MockBean
    private lateinit var quizService: QuizService

    @Test
    fun shouldCreateQuestion() {
        val quizId = UUID.randomUUID()

        `when`(quizService.createQuestion(any())).thenReturn(Mono.just(Unit))

        webClient
                .post()
                .uri("/api/quiz/$quizId/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(QuestionDTO(UUID.randomUUID(),"Warum?")))
                .exchange()
                .expectStatus().isCreated
    }

    @Test
    fun shouldReturnStatusConflictWhenCreatingAQuestionBecauseTheQuizIsFinished() {
        val quizId = UUID.randomUUID()

        `when`(quizService.createQuestion(any())).thenReturn(Mono.error(QuizFinishedException()))

        webClient
                .post()
                .uri("/api/quiz/$quizId/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(QuestionDTO(UUID.randomUUID(),"Warum?")))
                .exchange()
                .expectStatus().value { assertThat(it).isEqualTo(409) }
    }

    @Test
    fun shouldEditQuestion() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()

        `when`(quizService.editQuestion(any())).thenReturn(Mono.just(Unit))

        webClient
                .put()
                .uri("/api/quiz/$quizId/questions/$questionId")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(QuestionDTO(questionId,"Warum?")))
                .exchange()
                .expectStatus().isOk
    }

    @Test
    fun shouldReturnStatusConflictWhenEditingAQuestionBecauseTheQuizIsFinished() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()

        `when`(quizService.editQuestion(any())).thenReturn(Mono.error(QuizFinishedException()))

        webClient
                .put()
                .uri("/api/quiz/$quizId/questions/$questionId")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(QuestionDTO(questionId,"Warum?")))
                .exchange()
                .expectStatus().value { assertThat(it).isEqualTo(409) }
    }

    @Test
    fun shouldStartNewQuestion() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()

        `when`(quizService.startNewQuestion(any())).thenReturn(Mono.just(Unit))

        webClient
                .patch()
                .uri("/api/quiz/$quizId/questions/$questionId")
                .exchange()
                .expectStatus().isOk
    }

    @Test
    fun shouldReturnStatusConflictWhenStartingAQuestionBecauseTheQuizIsFinished() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()

        `when`(quizService.startNewQuestion(any())).thenReturn(Mono.error(QuizFinishedException()))

        webClient
                .patch()
                .uri("/api/quiz/$quizId/questions/$questionId")
                .exchange()
                .expectStatus().value { assertThat(it).isEqualTo(409) }
    }

    @Test
    fun shouldDeleteQuestion() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()

        `when`(quizService.deleteQuestion(any())).thenReturn(Mono.just(Unit))

        webClient
                .delete()
                .uri("/api/quiz/$quizId/questions/$questionId")
                .exchange()
                .expectStatus().isOk
    }

    @Test
    fun shouldReturnStatusConflictWhenDeletingAQuestionBecauseTheQuizIsFinished() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()

        `when`(quizService.deleteQuestion(any())).thenReturn(Mono.error(QuizFinishedException()))

        webClient
                .delete()
                .uri("/api/quiz/$quizId/questions/$questionId")
                .exchange()
                .expectStatus().value { assertThat(it).isEqualTo(409) }
    }

    private fun <T> any(): T {
        Mockito.any<T>()
        return null as T
    }

}