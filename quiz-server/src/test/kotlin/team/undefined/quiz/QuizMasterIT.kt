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
internal class QuizMasterIT {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun shouldCreateAndGetQuiz() {

        val quizMasterReference = AtomicReference<QuizDTO>()

        // Quiz is created
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
                .subscribe { quizMasterReference.set(it) }

        // First question is created
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

        // Second question is created
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

        // Third question is created
        webTestClient
                .post()
                .uri(quizMasterReference.get().getLink("createQuestion").map { it.href }.orElseThrow())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(QuestionDTO(question = "Was ist ein Robo-Advisor?", estimates = HashMap(), timeToAnswer = 45)))
                .exchange()
                .expectStatus().isCreated

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(3)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .isNotPending
                            .isBuzzerQuestion
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isBuzzerQuestion
                }
                .hasOpenQuestion(2) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .isNotPending
                            .isEstimationQuestion
                }
                .playedQuestionSizeIs(0)
                .particpantSizeIs(0)

        // First participant logs in
        webTestClient
                .post()
                .uri(quizMasterReference.get().getLink("createParticipant").map { it.href }.orElseThrow())
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("André"))
                .exchange()
                .expectStatus().isCreated

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(3)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .isNotPending
                            .isBuzzerQuestion
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isBuzzerQuestion
                }
                .hasOpenQuestion(2) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .isNotPending
                            .isEstimationQuestion
                }
                .playedQuestionSizeIs(0)
                .particpantSizeIs(1)
                .hasParticipant(0) { it.hasName("André").isNotTurn }

        // Second participant logs in
        webTestClient
                .post()
                .uri(quizMasterReference.get().getLink("createParticipant").map { it.href }.orElseThrow())
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("Lena"))
                .exchange()
                .expectStatus().isCreated

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(3)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .isNotPending
                            .isBuzzerQuestion
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isBuzzerQuestion
                }
                .hasOpenQuestion(2) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .isNotPending
                            .isEstimationQuestion
                }
                .playedQuestionSizeIs(0)
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").isNotTurn }

        // First buzzer question is asked
        webTestClient
                .patch()
                .uri(quizMasterReference.get().openQuestions[0].getLink("self").map { it.href }.orElseThrow())
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(3)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .isPending
                            .isBuzzerQuestion
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isBuzzerQuestion
                }
                .hasOpenQuestion(2) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .isNotPending
                            .isEstimationQuestion
                }
                .playedQuestionSizeIs(0)
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").isNotTurn }

        // First participant buzzers
        webTestClient
                .put()
                .uri(quizMasterReference.get().participants[1].getLink("buzzer").map { it.href }.orElseThrow())
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(3)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .isPending
                            .isBuzzerQuestion
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isBuzzerQuestion
                }
                .hasOpenQuestion(2) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .isNotPending
                            .isEstimationQuestion
                }
                .playedQuestionSizeIs(0)
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").isTurn }

        // Answer is correct
        webTestClient
                .post()
                .uri(quizMasterReference.get().getLink("answer-${quizMasterReference.get().participants[1].id}").map { it.href }.orElseThrow())
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("true"))
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(3)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .isPending
                            .isBuzzerQuestion
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isBuzzerQuestion
                }
                .hasOpenQuestion(2) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .isNotPending
                            .isEstimationQuestion
                }
                .playedQuestionSizeIs(0)
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").hasPoints(0).isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").hasPoints(2).isTurn }

        // Second buzzer question is asked
        webTestClient
                .patch()
                .uri(quizMasterReference.get().openQuestions[1].getLink("self").map { it.href }.orElseThrow())
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(2)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isPending
                            .isBuzzerQuestion
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .isNotPending
                            .isEstimationQuestion
                }
                .playedQuestionSizeIs(1)
                .hasPlayedQuestion(0) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .isNotPending
                            .isBuzzerQuestion
                }
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").hasPoints(0).isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").hasPoints(2).isNotTurn }

        // Participant buzzers
        webTestClient
                .put()
                .uri(quizMasterReference.get().participants[0].getLink("buzzer").map { it.href }.orElseThrow())
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(2)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isPending
                            .isBuzzerQuestion
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .isNotPending
                            .isEstimationQuestion
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

        // Answer is wrong
        webTestClient
                .post()
                .uri(quizMasterReference.get().getLink("answer-${quizMasterReference.get().participants[0].id}").map { it.href }.orElseThrow())
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("false"))
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(2)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isPending
                            .isBuzzerQuestion
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .isNotPending
                            .isEstimationQuestion
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

        // Login again with the same name
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
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isPending
                            .isBuzzerQuestion
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .isNotPending
                            .isEstimationQuestion
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

        // Ask estimation question
        webTestClient
                .patch()
                .uri(quizMasterReference.get().openQuestions[1].getLink("self").map { it.href }.orElseThrow())
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(1)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .isPending
                            .isEstimationQuestion
                }
                .playedQuestionSizeIs(2)
                .hasPlayedQuestion(0) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .isNotPending
                }
                .hasPlayedQuestion(1) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isBuzzerQuestion
                }
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").hasPoints(0).isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").hasPoints(2).isNotTurn }

        // Answer of participant 1
        webTestClient
                .put()
                .uri(quizMasterReference.get().participants[0].getLink("buzzer").map { it.href }.orElseThrow())
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("Antwort von André"))
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(1)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .isPending
                            .isEstimationQuestion
                            .hasEstimates(java.util.Map.of(quizMasterReference.get().participants[0].id, "Antwort von André"))
                }
                .playedQuestionSizeIs(2)
                .hasPlayedQuestion(0) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .isNotPending
                }
                .hasPlayedQuestion(1) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isBuzzerQuestion
                }
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").hasPoints(0).isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").hasPoints(2).isNotTurn }

        // Answer of participant 2
        webTestClient
                .put()
                .uri(quizMasterReference.get().participants[1].getLink("buzzer").map { it.href }.orElseThrow())
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("Antwort von Lena"))
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(1)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .isPending
                            .isEstimationQuestion
                            .hasEstimates(java.util.Map.of(
                                    quizMasterReference.get().participants[0].id, "Antwort von André",
                                    quizMasterReference.get().participants[1].id, "Antwort von Lena"
                            ))
                }
                .playedQuestionSizeIs(2)
                .hasPlayedQuestion(0) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .isNotPending
                }
                .hasPlayedQuestion(1) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isBuzzerQuestion
                }
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").hasPoints(0).isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").hasPoints(2).isNotTurn }

        // Answer of participant 1 was correct
        webTestClient
                .post()
                .uri(quizMasterReference.get().getLink("answer-${quizMasterReference.get().participants[0].id}").map { it.href }.orElseThrow())
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("true"))
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(1)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .isPending
                            .isEstimationQuestion
                            .hasEstimates(java.util.Map.of(
                                    quizMasterReference.get().participants[0].id, "Antwort von André",
                                    quizMasterReference.get().participants[1].id, "Antwort von Lena"
                            ))
                }
                .playedQuestionSizeIs(2)
                .hasPlayedQuestion(0) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .isNotPending
                }
                .hasPlayedQuestion(1) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isBuzzerQuestion
                }
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").hasPoints(2).isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").hasPoints(2).isNotTurn }
    }
}
