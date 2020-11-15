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
internal class QuizParticipantIT {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun shouldCreateAndGetQuiz() {

        val quizParticipantReference = AtomicReference<QuizDTO>()

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
        Thread.sleep(10)

        webTestClient.get()
                .uri("/api/quiz/${quizId}/quiz-participant")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk
                .returnResult(QuizDTO::class.java)
                .responseBody
                .subscribe { quizParticipantReference.set(it) }

        // First question is created
        webTestClient
                .post()
                .uri(quizParticipantReference.get().getLink("createQuestion").map { it.href }.orElseThrow())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(QuestionDTO(question = "Wer schrieb das Buch Animal Farm?")))
                .exchange()
                .expectStatus().isCreated

        assertThat(quizParticipantReference.get())
                .openQuestionSizeIs(1)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .isNotPending
                }
                .playedQuestionSizeIs(0)
                .particpantSizeIs(0)

        Thread.sleep(10)

        // Second question is created
        webTestClient
                .post()
                .uri(quizParticipantReference.get().getLink("createQuestion").map { it.href }.orElseThrow())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(QuestionDTO(question = "Wo befindet sich das Kahnbein?")))
                .exchange()
                .expectStatus().isCreated

        assertThat(quizParticipantReference.get())
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

        Thread.sleep(10)

        // Third question is created
        webTestClient
                .post()
                .uri(quizParticipantReference.get().getLink("createQuestion").map { it.href }.orElseThrow())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(QuestionDTO(question = "Was ist ein Robo-Advisor?", estimates = HashMap(), timeToAnswer = 45)))
                .exchange()
                .expectStatus().isCreated

        assertThat(quizParticipantReference.get())
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

        Thread.sleep(10)

        // First participant logs in
        webTestClient
                .post()
                .uri(quizParticipantReference.get().getLink("createParticipant").map { it.href }.orElseThrow())
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("André"))
                .exchange()
                .expectStatus().isCreated

        assertThat(quizParticipantReference.get())
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

        Thread.sleep(10)

        // Second participant logs in
        webTestClient
                .post()
                .uri(quizParticipantReference.get().getLink("createParticipant").map { it.href }.orElseThrow())
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("Lena"))
                .exchange()
                .expectStatus().isCreated

        assertThat(quizParticipantReference.get())
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

        Thread.sleep(10)

        // First buzzer question is asked
        webTestClient
                .patch()
                .uri(quizParticipantReference.get().openQuestions[0].getLink("self").map { it.href }.orElseThrow())
                .exchange()
                .expectStatus().isOk

        assertThat(quizParticipantReference.get())
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

        Thread.sleep(10)

        // First participant buzzers
        webTestClient
                .put()
                .uri(quizParticipantReference.get().participants[1].getLink("buzzer").map { it.href }.orElseThrow())
                .exchange()
                .expectStatus().isOk

        assertThat(quizParticipantReference.get())
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

        Thread.sleep(10)

        // Answer is correct
        webTestClient
                .post()
                .uri(quizParticipantReference.get().getLink("answer-${quizParticipantReference.get().participants[1].id}").map { it.href }.orElseThrow())
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("true"))
                .exchange()
                .expectStatus().isOk

        assertThat(quizParticipantReference.get())
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

        Thread.sleep(10)

        // Second buzzer question is asked
        webTestClient
                .patch()
                .uri(quizParticipantReference.get().openQuestions[1].getLink("self").map { it.href }.orElseThrow())
                .exchange()
                .expectStatus().isOk

        assertThat(quizParticipantReference.get())
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

        Thread.sleep(10)

        // Participant buzzers
        webTestClient
                .put()
                .uri(quizParticipantReference.get().participants[0].getLink("buzzer").map { it.href }.orElseThrow())
                .exchange()
                .expectStatus().isOk

        assertThat(quizParticipantReference.get())
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

        Thread.sleep(10)

        // Answer is wrong
        webTestClient
                .post()
                .uri(quizParticipantReference.get().getLink("answer-${quizParticipantReference.get().participants[0].id}").map { it.href }.orElseThrow())
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("false"))
                .exchange()
                .expectStatus().isOk

        assertThat(quizParticipantReference.get())
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

        Thread.sleep(10)

        // Login again with the same name
        webTestClient
                .post()
                .uri(quizParticipantReference.get().getLink("createParticipant").map { it.href }.orElseThrow())
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("Lena"))
                .exchange()
                .expectStatus().isCreated

        assertThat(quizParticipantReference.get())
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

        Thread.sleep(10)

        // Ask estimation question
        webTestClient
                .patch()
                .uri(quizParticipantReference.get().openQuestions[1].getLink("self").map { it.href }.orElseThrow())
                .exchange()
                .expectStatus().isOk

        assertThat(quizParticipantReference.get())
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

        Thread.sleep(10)

        // Answer of participant 1
        webTestClient
                .put()
                .uri(quizParticipantReference.get().participants[0].getLink("buzzer").map { it.href }.orElseThrow())
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("Antwort von André"))
                .exchange()
                .expectStatus().isOk

        assertThat(quizParticipantReference.get())
                .openQuestionSizeIs(1)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .isPending
                            .isEstimationQuestion
                            .hasEstimates(java.util.Map.of(quizParticipantReference.get().participants[0].id, "*****"))
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

        Thread.sleep(10)

        // Participant 2 does not want the answer to be revealed
        webTestClient
                .put()
                .uri(quizParticipantReference.get().participants[1].getLink("toggleRevealAllowed").map { it.href }.orElseThrow())
                .exchange()
                .expectStatus().isOk

        assertThat(quizParticipantReference.get())
                .openQuestionSizeIs(1)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .isPending
                            .isEstimationQuestion
                            .hasEstimates(java.util.Map.of(quizParticipantReference.get().participants[0].id, "*****"))
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

        Thread.sleep(10)

        // Answer of participant 2
        webTestClient
                .put()
                .uri(quizParticipantReference.get().participants[1].getLink("buzzer").map { it.href }.orElseThrow())
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("Antwort von Lena"))
                .exchange()
                .expectStatus().isOk

        assertThat(quizParticipantReference.get())
                .openQuestionSizeIs(1)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .isPending
                            .isEstimationQuestion
                            .hasEstimates(java.util.Map.of(
                                    quizParticipantReference.get().participants[0].id, "*****",
                                    quizParticipantReference.get().participants[1].id, "*****"
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

        Thread.sleep(10)

        // Answers are revealed
        webTestClient
                .patch()
                .uri(quizParticipantReference.get().getLink("revealAnswers").map { it.href }.orElseThrow())
                .exchange()
                .expectStatus().isOk

        assertThat(quizParticipantReference.get())
                .openQuestionSizeIs(1)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .isPending
                            .isEstimationQuestion
                            .hasEstimates(java.util.Map.of(
                                    quizParticipantReference.get().participants[0].id, "Antwort von André",
                                    quizParticipantReference.get().participants[1].id, "*****"
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

        Thread.sleep(10)

        // Answer of participant 1 was correct
        webTestClient
                .post()
                .uri(quizParticipantReference.get().getLink("answer-${quizParticipantReference.get().participants[0].id}").map { it.href }.orElseThrow())
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("true"))
                .exchange()
                .expectStatus().isOk

        assertThat(quizParticipantReference.get())
                .openQuestionSizeIs(1)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .isPending
                            .isEstimationQuestion
                            .hasEstimates(java.util.Map.of(
                                    quizParticipantReference.get().participants[0].id, "Antwort von André",
                                    quizParticipantReference.get().participants[1].id, "*****"
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
