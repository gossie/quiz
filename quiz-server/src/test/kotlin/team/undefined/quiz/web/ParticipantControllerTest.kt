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

@WebFluxTest(controllers = [ParticipantsController::class])
internal class ParticipantControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient
    @MockBean
    private lateinit var  quizService: QuizService
    @MockBean
    private lateinit var quizProjection: QuizProjection // TODO: dich möchte ich wieder loswerden

    @Test
    fun shouldCreateParticipant() {
        val quizId = UUID.randomUUID()

        `when`(quizService.createParticipant(CreateParticipantCommand(quizId, Participant(name = "Erik"))))
                .thenReturn(Mono.just(Unit))

        webTestClient
                .post()
                .uri("/api/quiz/$quizId/participants")
                .contentType(MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue("Erik"))
                .exchange()
                .expectStatus().isCreated
    }

    @Test
    fun shouldBuzzer() {
        val quizId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        `when`(quizService.buzzer(BuzzerCommand(quizId, participantId)))
                .thenReturn(Mono.just(Unit))

        webTestClient
                .put()
                .uri("/api/quiz/$quizId/participants/$participantId/buzzer")
                .exchange()
                .expectStatus().isOk
    }
}