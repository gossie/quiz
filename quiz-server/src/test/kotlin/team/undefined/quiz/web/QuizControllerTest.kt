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
import team.undefined.quiz.core.Question
import team.undefined.quiz.core.Quiz
import team.undefined.quiz.core.QuizService

@WebFluxTest(controllers = [QuizController::class])
@Import(WebSocketHandlerFactory::class)
internal class QuizControllerTest {

    @Autowired private lateinit var webTestClient: WebTestClient
    @MockBean private lateinit var  quizService: QuizService

    @Test
    fun shouldCreateQuiz() {
        `when`(quizService.createQuiz(Quiz(name = "Q"))).thenReturn(Mono.just(Quiz(17, "Q")))
        `when`(quizService.determineQuiz(17)).thenReturn(Mono.just(Quiz(17, "Q")))

        webTestClient
                .post()
                .uri("/api/quiz")
                .body(BodyInserters.fromValue(QuizDTO(name = "Q")))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated
                .expectBody().json("{\"name\":\"Q\",\"links\":[{\"href\":\"/api/quiz/17/participants\",\"rel\":\"createParticipant\"},{\"href\":\"/api/quiz/17/questions\",\"rel\":\"createQuestion\"},{\"href\":\"/api/quiz/17\",\"rel\":\"answer\"}]}")
    }

    @Test
    fun shouldGetQuiz() {
        `when`(quizService.determineQuiz(17)).thenReturn(Mono.just(Quiz(17, "Q")))

        val result = webTestClient
                .get()
                .uri("/api/quiz/17")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody(QuizDTO::class.java)
                .returnResult()
                .responseBody

        assertThat(result?.id).isEqualTo(17)
        assertThat(result?.name).isEqualTo("Q")
        assertThat(result?.participants).isEmpty()
        assertThat(result?.questions).isEmpty()
        assertThat(result?.links).hasSize(3)
        assertThat(result?.getLink("createParticipant"))
                .map { it.href }
                .contains("/api/quiz/17/participants")
        assertThat(result?.getLink("createQuestion"))
                .map { it.href }
                .contains("/api/quiz/17/questions")
        assertThat(result?.getLink("answer"))
                .map { it.href }
                .contains("/api/quiz/17")
    }

    @Test
    fun shouldAnswerCorrect() {
        `when`(quizService.correctAnswer(14)).thenReturn(Mono.just(Quiz(14, "Quiz", listOf(Participant(17, "Sandra", false, 1), Participant(18, "Erik", false, 0)), listOf(Question(19, "Warum ist die Banane krumm?")))))

        val result = webTestClient
                .patch()
                .uri("/api/quiz/14")
                .contentType(MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue("true"))
                .exchange()
                .expectStatus().isOk
                .expectBody(QuizDTO::class.java)
                .returnResult()
                .responseBody

        assertThat(result!!.id).isEqualTo(14)
        assertThat(result.name).isEqualTo("Quiz")
        assertThat(result.participants).hasSize(2)
        assertThat(result.participants[0]).isEqualTo(ParticipantDTO(17, "Sandra", false, 1))
        assertThat(result.participants[1]).isEqualTo(ParticipantDTO(18, "Erik", false, 0))
        assertThat(result.questions).isEqualTo(listOf("Warum ist die Banane krumm?"))
        assertThat(result.links).hasSize(3)
        assertThat(result.getLink("createParticipant"))
                .map { it.href }
                .contains("/api/quiz/14/participants")
        assertThat(result.getLink("createQuestion"))
                .map { it.href }
                .contains("/api/quiz/14/questions")
        assertThat(result.getLink("answer"))
                .map { it.href }
                .contains("/api/quiz/14")
    }

    @Test
    fun shouldAnswerIncorrect() {
        `when`(quizService.incorrectAnswer(14)).thenReturn(Mono.just(Quiz(14, "Quiz", listOf(Participant(17, "Sandra", false, 1), Participant(18, "Erik", false, 0)), listOf(Question(19, "Warum ist die Banane krumm?")))))

        val result = webTestClient
                .patch()
                .uri("/api/quiz/14")
                .contentType(MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue("false"))
                .exchange()
                .expectStatus().isOk
                .expectBody(QuizDTO::class.java)
                .returnResult()
                .responseBody

        assertThat(result!!.id).isEqualTo(14)
        assertThat(result.name).isEqualTo("Quiz")
        assertThat(result.participants).hasSize(2)
        assertThat(result.participants[0]).isEqualTo(ParticipantDTO(17, "Sandra", false, 1))
        assertThat(result.participants[1]).isEqualTo(ParticipantDTO(18, "Erik", false, 0))
        assertThat(result.questions).isEqualTo(listOf("Warum ist die Banane krumm?"))
        assertThat(result.links).hasSize(3)
        assertThat(result.getLink("createParticipant"))
                .map { it.href }
                .contains("/api/quiz/14/participants")
        assertThat(result.getLink("createQuestion"))
                .map { it.href }
                .contains("/api/quiz/14/questions")
        assertThat(result.getLink("answer"))
                .map { it.href }
                .contains("/api/quiz/14")
    }

}