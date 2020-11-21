package team.undefined.quiz.web

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Mono
import team.undefined.quiz.core.*
import java.util.*

@WebFluxTest(controllers = [ParticipantsController::class])
internal class ParticipantsControllerTest {

    @Autowired
    private lateinit var webClient: WebTestClient

    @MockBean
    private lateinit var quizService: QuizService

    @Test
    fun shouldCreateParticipant() {
        val quizId = UUID.randomUUID()

        `when`(quizService.createParticipant(any())).thenReturn(Mono.just(Unit))

        webClient
                .post()
                .uri("/api/quiz/$quizId/participants")
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("Allli"))
                .exchange()
                .expectStatus().isCreated
    }

    @Test
    fun shouldReturnStatusConflictWhenCreatingAParticipantBecauseTheQuizIsFinished() {
        val quizId = UUID.randomUUID()

        `when`(quizService.createParticipant(any())).thenReturn(Mono.error(QuizFinishedException()))

        webClient
                .post()
                .uri("/api/quiz/$quizId/participants")
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("Allli"))
                .exchange()
                .expectStatus().value { assertThat(it).isEqualTo(409) }
    }

    @Test
    fun shouldReturnStatusNotFoundWhenCreatingAParticipantBecauseTheQuizIsNotFound() {
        val quizId = UUID.randomUUID()

        `when`(quizService.createParticipant(any())).thenReturn(Mono.error(QuizNotFoundException()))

        webClient
                .post()
                .uri("/api/quiz/$quizId/participants")
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("Allli"))
                .exchange()
                .expectStatus().isNotFound
    }

    @Test
    fun shouldBuzzer() {
        val quizId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        `when`(quizService.buzzer(BuzzerCommand(quizId, participantId))).thenReturn(Mono.just(Unit))

        webClient
                .put()
                .uri("/api/quiz/$quizId/participants/$participantId/buzzer")
                .exchange()
                .expectStatus().isOk
    }

    @Test
    fun shouldReturnStatusConflictWhenBuzzeringBecauseTheQuizIsFinished() {
        val quizId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        `when`(quizService.buzzer(BuzzerCommand(quizId, participantId))).thenReturn(Mono.error(QuizFinishedException()))

        webClient
                .put()
                .uri("/api/quiz/$quizId/participants/$participantId/buzzer")
                .exchange()
                .expectStatus().value { assertThat(it).isEqualTo(409) }
    }

    @Test
    fun shouldEstimate() {
        val quizId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        `when`(quizService.estimate(EstimationCommand(quizId, participantId, "Antwort"))).thenReturn(Mono.just(Unit))

        webClient
                .put()
                .uri("/api/quiz/$quizId/participants/$participantId/buzzer")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue("Antwort")
                .exchange()
                .expectStatus().isOk
    }

    @Test
    fun shouldReturnStatusConflictWhenEstimatingBecauseTheQuizIsFinished() {
        val quizId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        `when`(quizService.estimate(EstimationCommand(quizId, participantId, "Antwort"))).thenReturn(Mono.error(QuizFinishedException()))

        webClient
                .put()
                .uri("/api/quiz/$quizId/participants/$participantId/buzzer")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue("Antwort")
                .exchange()
                .expectStatus().value { assertThat(it).isEqualTo(409) }
    }

    @Test
    fun shouldToggle() {
        val quizId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        `when`(quizService.toggleAnswerRevealAllowed(any())).thenReturn(Mono.just(Unit))

        webClient
                .put()
                .uri("/api/quiz/$quizId/participants/$participantId/togglereveal")
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("Allli"))
                .exchange()
                .expectStatus().isOk
    }

    @Test
    fun shouldReturnStatusConflictWhenTogglingBecauseTheQuizIsFinished() {
        val quizId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        `when`(quizService.toggleAnswerRevealAllowed(any())).thenReturn(Mono.error(QuizFinishedException()))

        webClient
                .put()
                .uri("/api/quiz/$quizId/participants/$participantId/togglereveal")
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("Allli"))
                .exchange()
                .expectStatus().value { assertThat(it).isEqualTo(409) }
    }

    @Test
    fun shouldDeleteParticipant() {
        val quizId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        `when`(quizService.deleteParticipant(any())).thenReturn(Mono.just(Unit))

        webClient
                .delete()
                .uri("/api/quiz/$quizId/participants/$participantId")
                .exchange()
                .expectStatus().isOk
    }

    @Test
    fun shouldReturnStatusConflictWhenDeletingBecauseTheQuizIsFinished() {
        val quizId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        `when`(quizService.deleteParticipant(any())).thenReturn(Mono.error(QuizFinishedException()))

        webClient
                .delete()
                .uri("/api/quiz/$quizId/participants/$participantId")
                .exchange()
                .expectStatus().value { assertThat(it).isEqualTo(409) }
    }

    private fun <T> any(): T {
        Mockito.any<T>()
        return null as T
    }

}