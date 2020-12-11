package team.undefined.quiz.web

import com.google.common.eventbus.EventBus
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
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

@WebFluxTest(controllers = [QuizController::class])
internal class QuizControllerTest {

    @Autowired
    private lateinit var webClient: WebTestClient

    @MockBean
    private lateinit var quizService: QuizService

    @MockBean
    private lateinit var quizProjection: QuizProjection

    @MockBean
    private lateinit var eventBus: EventBus

    @Test
    fun shouldAnswerCorrect() {
        val quizId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        `when`(quizService.rate(AnswerCommand(quizId, participantId, AnswerCommand.Answer.CORRECT))).thenReturn(Mono.just(Unit))

        webClient
                .post()
                .uri("/api/quiz/$quizId/participants/$participantId/answers")
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("true"))
                .exchange()
                .expectStatus().isOk
    }

    @Test
    fun shouldReturnStatusConflictWhenAnsweringCorrectBecauseTheQuizIsFinished() {
        val quizId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        `when`(quizService.rate(AnswerCommand(quizId, participantId, AnswerCommand.Answer.CORRECT))).thenReturn(Mono.error(QuizFinishedException()))

        webClient
                .post()
                .uri("/api/quiz/$quizId/participants/$participantId/answers")
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("true"))
                .exchange()
                .expectStatus().value { Assertions.assertThat(it).isEqualTo(409) }
    }

    @Test
    fun shouldAnswerIncorrect() {
        val quizId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        `when`(quizService.rate(AnswerCommand(quizId, participantId, AnswerCommand.Answer.INCORRECT))).thenReturn(Mono.just(Unit))

        webClient
                .post()
                .uri("/api/quiz/$quizId/participants/$participantId/answers")
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("false"))
                .exchange()
                .expectStatus().isOk
    }

    @Test
    fun shouldReturnStatusConflictWhenAnsweringIncorrectBecauseTheQuizIsFinished() {
        val quizId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        `when`(quizService.rate(AnswerCommand(quizId, participantId, AnswerCommand.Answer.INCORRECT))).thenReturn(Mono.error(QuizFinishedException()))

        webClient
                .post()
                .uri("/api/quiz/$quizId/participants/$participantId/answers")
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("false"))
                .exchange()
                .expectStatus().value { Assertions.assertThat(it).isEqualTo(409) }
    }

    @Test
    fun shouldReopenQuestion() {
        val quizId = UUID.randomUUID()

        `when`(quizService.reopenQuestion(ReopenCurrentQuestionCommand(quizId))).thenReturn(Mono.just(Unit))

        webClient
                .put()
                .uri("/api/quiz/$quizId")
                .exchange()
                .expectStatus().isOk
    }

    @Test
    fun shouldReturnStatusConflictWhenReopeningQuestionBecauseTheQuizIsFinished() {
        val quizId = UUID.randomUUID()

        `when`(quizService.reopenQuestion(ReopenCurrentQuestionCommand(quizId))).thenReturn(Mono.error(QuizFinishedException()))

        webClient
                .put()
                .uri("/api/quiz/$quizId")
                .exchange()
                .expectStatus().value { Assertions.assertThat(it).isEqualTo(409) }
    }

    @Test
    fun shouldRevealAnswers() {
        val quizId = UUID.randomUUID()

        `when`(quizService.revealAnswers(RevealAnswersCommand(quizId))).thenReturn(Mono.just(Unit))

        webClient
                .patch()
                .uri("/api/quiz/$quizId")
                .exchange()
                .expectStatus().isOk
    }

    @Test
    fun shouldReturnStatusConflictWhenRevealingAnswersBecauseTheQuizIsFinished() {
        val quizId = UUID.randomUUID()

        `when`(quizService.revealAnswers(RevealAnswersCommand(quizId))).thenReturn(Mono.error(QuizFinishedException()))

        webClient
                .patch()
                .uri("/api/quiz/$quizId")
                .exchange()
                .expectStatus().value { Assertions.assertThat(it).isEqualTo(409) }
    }

}
