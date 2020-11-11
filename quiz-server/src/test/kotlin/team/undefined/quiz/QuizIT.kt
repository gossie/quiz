package team.undefined.quiz

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.reactive.function.BodyInserters
import team.undefined.quiz.web.QuestionDTO
import team.undefined.quiz.web.QuizDTO
import team.undefined.quiz.web.QuizDTOAssert.assertThat
import java.util.*
import java.util.concurrent.atomic.AtomicReference

@SpringBootTest
@AutoConfigureWebTestClient
internal class QuizIT {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun shouldCreateAndGetQuiz() {

        val quizMasterReference = AtomicReference<QuizDTO>()

        val quizId = webTestClient
                .post()
                .uri("/api/quiz")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue(QuizDTO(name = "Quiz", timestamp = Date().time)))
                .exchange()
                .expectStatus().isCreated
                .expectBody<String>()
                .returnResult()
                .responseBody

        assertThat(quizId).isNotNull()

        webTestClient.get()
                .uri("/api/quiz/${quizId}/quiz-master")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk
                .returnResult(QuizDTO::class.java)
                .responseBody
                .subscribe {
                    println("received for quiz-master: $it")
                    quizMasterReference.set(it)
                }

        /*
        webTestClient.get()
                .uri("/api/quiz/${quizId}/quiz-participant")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk
                .returnResult(QuizDTO::class.java)
                .responseBody
                .subscribe {
                    println("received for quiz-participant: $it")
                    quizParticipantReference.set(it)
                }
        */

        webTestClient
                .post()
                .uri(quizMasterReference.get().getLink("createQuestion").map { it.href }.orElseThrow())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(QuestionDTO(question = "Wer schrieb das Buch Animal Farm?")))
                .exchange()
                .expectStatus().isCreated

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(1)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .isNotPending
                }
                .playedQuestionSizeIs(0)
                .particpantSizeIs(0)

        webTestClient
                .post()
                .uri(quizMasterReference.get().getLink("createQuestion").map { it.href }.orElseThrow())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(QuestionDTO(question = "Wo befindet sich das Kahnbein?")))
                .exchange()
                .expectStatus().isCreated

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(2)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .isNotPending
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                }
                .playedQuestionSizeIs(0)
                .particpantSizeIs(0)

        webTestClient
                .post()
                .uri(quizMasterReference.get().getLink("createParticipant").map { it.href }.orElseThrow())
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("André"))
                .exchange()
                .expectStatus().isCreated

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(2)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .isNotPending
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                }
                .playedQuestionSizeIs(0)
                .particpantSizeIs(1)
                .hasParticipant(0) { it.hasName("André").isNotTurn }

        webTestClient
                .post()
                .uri(quizMasterReference.get().getLink("createParticipant").map { it.href }.orElseThrow())
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("Lena"))
                .exchange()
                .expectStatus().isCreated

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(2)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .isNotPending
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                }
                .playedQuestionSizeIs(0)
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").isNotTurn }

        webTestClient
                .patch()
                .uri(quizMasterReference.get().openQuestions[0].getLink("self").map { it.href }.orElseThrow())
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(2)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .isPending
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                }
                .playedQuestionSizeIs(0)
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").isNotTurn }

        webTestClient
                .put()
                .uri(quizMasterReference.get().participants[1].getLink("buzzer").map { it.href }.orElseThrow())
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(2)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .isPending
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                }
                .playedQuestionSizeIs(0)
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").isTurn }

        webTestClient
                .post()
                .uri(quizMasterReference.get().getLink("answer-${quizMasterReference.get().participants[1].id}").map { it.href }.orElseThrow())
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("true"))
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(2)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .isPending
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                }
                .playedQuestionSizeIs(0)
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").hasPoints(0).isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").hasPoints(2).isTurn }

        webTestClient
                .patch()
                .uri(quizMasterReference.get().openQuestions[1].getLink("self").map { it.href }.orElseThrow())
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(1)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isPending
                }
                .playedQuestionSizeIs(1)
                .hasPlayedQuestion(0) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .isNotPending
                }
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").hasPoints(0).isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").hasPoints(2).isNotTurn }

        webTestClient
                .put()
                .uri(quizMasterReference.get().participants[0].getLink("buzzer").map { it.href }.orElseThrow())
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(1)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isPending
                }
                .playedQuestionSizeIs(1)
                .hasPlayedQuestion(0) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .isNotPending
                }
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").hasPoints(0).isTurn }
                .hasParticipant(1) { it.hasName("Lena").hasPoints(2).isNotTurn }

        webTestClient
                .post()
                .uri(quizMasterReference.get().getLink("answer-${quizMasterReference.get().participants[0].id}").map { it.href }.orElseThrow())
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("false"))
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(1)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isPending
                }
                .playedQuestionSizeIs(1)
                .hasPlayedQuestion(0) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .isNotPending
                }
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").hasPoints(0).isTurn }
                .hasParticipant(1) { it.hasName("Lena").hasPoints(2).isNotTurn }

        webTestClient
                .post()
                .uri(quizMasterReference.get().getLink("createParticipant").map { it.href }.orElseThrow())
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("Lena"))
                .exchange()
                .expectStatus().isCreated

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(1)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isPending
                }
                .playedQuestionSizeIs(1)
                .hasPlayedQuestion(0) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .isNotPending
                }
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").hasPoints(0).isTurn }
                .hasParticipant(1) { it.hasName("Lena").hasPoints(2).isNotTurn }
    }
}
