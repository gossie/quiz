package team.undefined.quiz.web

import org.assertj.core.api.Assertions.assertThat
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
import team.undefined.quiz.core.Quiz
import team.undefined.quiz.core.QuizService

@WebFluxTest(controllers = [ParticipantsController::class])
@Import(ReactiveWebSocketHandler::class)
internal class ParticipantControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient
    @MockBean
    private lateinit var  quizService: QuizService

    @Test
    fun shouldCreateParticipant() {
        `when`(quizService.createParticipant(7, "Erik"))
                .thenReturn(Mono.just(Quiz(7, "Quiz", listOf(Participant(23, "Erik")))))

        val result = webTestClient
                .post()
                .uri("/api/quiz/7/participants")
                .contentType(MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue("Erik"))
                .exchange()
                .expectStatus().isCreated
                .expectBody(CreateParticipantResponse::class.java)
                .returnResult()
                .responseBody

        val quizDTO = result!!.quiz
        val participantId = result.participantId

        assertThat(participantId).isEqualTo(23)
        assertThat(quizDTO.id).isEqualTo(7)
        assertThat(quizDTO.name).isEqualTo("Quiz")
        assertThat(quizDTO.participants).hasSize(1)
        assertThat(quizDTO.participants[0]).isEqualTo(ParticipantDTO(23, "Erik", false, 0))
        assertThat(quizDTO.participants[0].getLink("buzzer"))
                .map{ it.href }
                .contains("/api/quiz/7/participants/23/buzzer")
        assertThat(quizDTO.openQuestions).isEmpty()
        assertThat(quizDTO.playedQuestions).isEmpty()
        assertThat(quizDTO.links).hasSize(4)
        assertThat(quizDTO.getLink("createParticipant"))
                .map { it.href }
                .contains("/api/quiz/7/participants")
        assertThat(quizDTO.getLink("createQuestion"))
                .map { it.href }
                .contains("/api/quiz/7/questions")
        assertThat(quizDTO.getLink("answer"))
                .map { it.href }
                .contains("/api/quiz/7")
        assertThat(quizDTO.getLink("reopenQuestion"))
                .map { it.href }
                .contains("/api/quiz/7")
    }

    @Test
    fun shouldBuzzer() {
        `when`(quizService.buzzer(17, 23))
                .thenReturn(Mono.just(Quiz(17, "Quiz", listOf(Participant(23, "Sandra", true), Participant(24, "Allli"), Participant(25, "Erik")), emptyList())))

        webTestClient
                .put()
                .uri("/api/quiz/17/participants/23/buzzer")
                .exchange()
                .expectStatus().isOk
    }
}