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
import team.undefined.quiz.web.ChoiceDTO
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

        assertThat(quizId).isNotNull

        Thread.sleep(10)

        webTestClient.get()
                .uri("/api/quiz/${quizId}/quiz-master")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk
                .returnResult(QuizDTO::class.java)
                .responseBody
                .subscribe { quizMasterReference.set(it) }

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(0)
                .playedQuestionSizeIs(0)
                .particpantSizeIs(0)
                .undoIsNotPossible()
                .redoIsNotPossible()
                .hasExpirationDate()
                .isNotFinished

        Thread.sleep(10)

        // First question is created
        webTestClient
                .post()
                .uri(quizMasterReference.get().getLink("createQuestion").map { it.href }.orElseThrow())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(QuestionDTO(question = "Wer schrieb das Buch Animal Farm?", correctAnswer = "George Orwell")))
                .exchange()
                .expectStatus().isCreated

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(1)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .hasAnswerNote("George Orwell")
                            .isNotPending
                }
                .playedQuestionSizeIs(0)
                .particpantSizeIs(0)
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        Thread.sleep(10)

        // Second question is created
        webTestClient
                .post()
                .uri(quizMasterReference.get().getLink("createQuestion").map { it.href }.orElseThrow())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(QuestionDTO(question = "Wo befindet sich das Kahnbein", choices = listOf(ChoiceDTO(choice = "Im Fuß"), ChoiceDTO(choice = "In der Hand")), estimates = HashMap(), points = 4)))
                .exchange()
                .expectStatus().isCreated

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(2)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .hasAnswerNote("George Orwell")
                            .isNotPending
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein")
                            .isNotPending
                            .isMultipleChoiceQuestion
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
                }
                .playedQuestionSizeIs(0)
                .particpantSizeIs(0)
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        Thread.sleep(10)

        // Second question is edited
        webTestClient
                .put()
                .uri(quizMasterReference.get().openQuestions[1].getLink("self").map { it.href }.orElseThrow())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(QuestionDTO(question = "Wo befindet sich das Kahnbein?", choices = listOf(ChoiceDTO(choice = "Im Fuß"), ChoiceDTO(choice = "In der Hand")), estimates = HashMap(), points = 4, previousQuestionId = quizMasterReference.get().openQuestions[0].id)))
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(2)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .hasAnswerNote("George Orwell")
                            .isNotPending
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isMultipleChoiceQuestion
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
                }
                .playedQuestionSizeIs(0)
                .particpantSizeIs(0)
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        Thread.sleep(10)

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
                            .hasAnswerNote("George Orwell")
                            .isNotPending
                            .isBuzzerQuestion
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isMultipleChoiceQuestion
                }
                .hasOpenQuestion(2) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .isNotPending
                            .isEstimationQuestion
                }
                .playedQuestionSizeIs(0)
                .particpantSizeIs(0)
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        Thread.sleep(10)

        // Third question is created
        webTestClient
                .put()
                .uri(quizMasterReference.get().openQuestions[2].getLink("self").map { it.href }.orElseThrow())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(QuestionDTO(question = "Was ist ein Robo-Advisor?", correctAnswer = "Ein algorithmen gesteuertes Dings", estimates = HashMap(), timeToAnswer = 45, previousQuestionId = quizMasterReference.get().openQuestions[1].id)))
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(3)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .hasAnswerNote("George Orwell")
                            .isNotPending
                            .isBuzzerQuestion
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isMultipleChoiceQuestion
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
                }
                .hasOpenQuestion(2) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .isNotPending
                            .isEstimationQuestion
                }
                .playedQuestionSizeIs(0)
                .particpantSizeIs(0)
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        Thread.sleep(10)

        // change order of questions
        webTestClient
                .put()
                .uri(quizMasterReference.get().openQuestions[2].getLink("self").map { it.href }.orElseThrow())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(QuestionDTO(question = "Was ist ein Robo-Advisor?", correctAnswer = "Ein algorithmen gesteuertes Dings", estimates = HashMap(), timeToAnswer = 45, previousQuestionId = quizMasterReference.get().openQuestions[0].id)))
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(3)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .hasAnswerNote("George Orwell")
                            .isNotPending
                            .isBuzzerQuestion
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .hasAnswerNote("Ein algorithmen gesteuertes Dings")
                            .isNotPending
                            .isEstimationQuestion
                }
                .hasOpenQuestion(2) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isMultipleChoiceQuestion
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
                }
                .playedQuestionSizeIs(0)
                .particpantSizeIs(0)
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        Thread.sleep(10)

        // change it back
        webTestClient
                .put()
                .uri(quizMasterReference.get().openQuestions[1].getLink("self").map { it.href }.orElseThrow())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(QuestionDTO(question = "Was ist ein Robo-Advisor?", correctAnswer = "Ein algorithmen gesteuertes Dings", estimates = HashMap(), timeToAnswer = 45, previousQuestionId = quizMasterReference.get().openQuestions[2].id)))
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(3)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .hasAnswerNote("George Orwell")
                            .isNotPending
                            .isBuzzerQuestion
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isMultipleChoiceQuestion
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
                }
                .hasOpenQuestion(2) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .hasAnswerNote("Ein algorithmen gesteuertes Dings")
                            .isNotPending
                            .isEstimationQuestion
                }
                .playedQuestionSizeIs(0)
                .particpantSizeIs(0)
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        Thread.sleep(10)

        // Fourth question is created
        webTestClient
                .post()
                .uri(quizMasterReference.get().getLink("createQuestion").map { it.href }.orElseThrow())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(QuestionDTO(question = "Wird diese Frage wieder gelöscht?", estimates = HashMap(), timeToAnswer = 45)))
                .exchange()
                .expectStatus().isCreated

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(4)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .hasAnswerNote("George Orwell")
                            .isNotPending
                            .isBuzzerQuestion
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isMultipleChoiceQuestion
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
                }
                .hasOpenQuestion(2) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .hasAnswerNote("Ein algorithmen gesteuertes Dings")
                            .isNotPending
                            .isEstimationQuestion
                }
                .hasOpenQuestion(3) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wird diese Frage wieder gelöscht?")
                            .isNotPending
                            .isEstimationQuestion
                }
                .playedQuestionSizeIs(0)
                .particpantSizeIs(0)
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        Thread.sleep(10)

        // Fourth question is deleted
        webTestClient
                .delete()
                .uri(quizMasterReference.get().openQuestions[3].getLink("self").map { it.href }.orElseThrow())
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(3)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .hasAnswerNote("George Orwell")
                            .isNotPending
                            .isBuzzerQuestion
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isMultipleChoiceQuestion
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
                }
                .hasOpenQuestion(2) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .hasAnswerNote("Ein algorithmen gesteuertes Dings")
                            .isNotPending
                            .isEstimationQuestion
                }
                .playedQuestionSizeIs(0)
                .particpantSizeIs(0)
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        Thread.sleep(10)

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
                            .hasAnswerNote("George Orwell")
                            .isNotPending
                            .isBuzzerQuestion
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isMultipleChoiceQuestion
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
                }
                .hasOpenQuestion(2) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .hasAnswerNote("Ein algorithmen gesteuertes Dings")
                            .isNotPending
                            .isEstimationQuestion
                }
                .playedQuestionSizeIs(0)
                .particpantSizeIs(1)
                .hasParticipant(0) { it.hasName("André").allowsReveal().isNotTurn }
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        Thread.sleep(10)

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
                            .hasAnswerNote("George Orwell")
                            .isNotPending
                            .isBuzzerQuestion
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isMultipleChoiceQuestion
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
                }
                .hasOpenQuestion(2) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .hasAnswerNote("Ein algorithmen gesteuertes Dings")
                            .isNotPending
                            .isEstimationQuestion
                }
                .playedQuestionSizeIs(0)
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").allowsReveal().isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").allowsReveal().isNotTurn }
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        Thread.sleep(10)

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
                            .hasAnswerNote("George Orwell")
                            .isPending
                            .isBuzzerQuestion
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isMultipleChoiceQuestion
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
                }
                .hasOpenQuestion(2) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .hasAnswerNote("Ein algorithmen gesteuertes Dings")
                            .isNotPending
                            .isEstimationQuestion
                }
                .playedQuestionSizeIs(0)
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").allowsReveal().isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").allowsReveal().isNotTurn }
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        Thread.sleep(10)

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
                            .hasAnswerNote("George Orwell")
                            .isPending
                            .isBuzzerQuestion
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isMultipleChoiceQuestion
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
                }
                .hasOpenQuestion(2) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .hasAnswerNote("Ein algorithmen gesteuertes Dings")
                            .isNotPending
                            .isEstimationQuestion
                }
                .playedQuestionSizeIs(0)
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").allowsReveal().isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").allowsReveal().isTurn }
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        Thread.sleep(10)

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
                            .hasAnswerNote("George Orwell")
                            .isPending
                            .isBuzzerQuestion
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isMultipleChoiceQuestion
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
                }
                .hasOpenQuestion(2) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .hasAnswerNote("Ein algorithmen gesteuertes Dings")
                            .isNotPending
                            .isEstimationQuestion
                }
                .playedQuestionSizeIs(0)
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").hasPoints(0).allowsReveal().isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").hasPoints(2).allowsReveal().isTurn }
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        Thread.sleep(10)

        // Multiple choice question is asked
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
                            .isMultipleChoiceQuestion
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .hasAnswerNote("Ein algorithmen gesteuertes Dings")
                            .isNotPending
                            .isEstimationQuestion
                }
                .playedQuestionSizeIs(1)
                .hasPlayedQuestion(0) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .hasAnswerNote("George Orwell")
                            .isNotPending
                            .isBuzzerQuestion
                }
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").hasPoints(0).allowsReveal().isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").hasPoints(2).allowsReveal().isNotTurn }
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        Thread.sleep(10)

        // Participant 1 answers
        webTestClient
                .put()
                .uri(quizMasterReference.get().openQuestions[0].choices!![0].getLink("${quizMasterReference.get().participants[0].id}-selects-choice").map { it.href }.orElseThrow())
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(2)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isPending
                            .isMultipleChoiceQuestion
                            .hasEstimates(mapOf(Pair(quizMasterReference.get().participants[0].id, "Im Fuß")))
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .hasAnswerNote("Ein algorithmen gesteuertes Dings")
                            .isNotPending
                            .isEstimationQuestion
                }
                .playedQuestionSizeIs(1)
                .hasPlayedQuestion(0) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .hasAnswerNote("George Orwell")
                            .isNotPending
                }
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").hasPoints(0).allowsReveal().isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").hasPoints(2).allowsReveal().isNotTurn }
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        Thread.sleep(10)

        // Participant 2 answers
        webTestClient
                .put()
                .uri(quizMasterReference.get().openQuestions[0].choices!![0].getLink("${quizMasterReference.get().participants[1].id}-selects-choice").map { it.href }.orElseThrow())
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(2)
                .hasOpenQuestion(0) { openQuestion ->
                        openQuestion
                                .hasQuestion("Wo befindet sich das Kahnbein?")
                                .isPending
                                .isMultipleChoiceQuestion
                                .hasEstimates(mapOf(
                                        Pair(quizMasterReference.get().participants[0].id, "Im Fuß"),
                                        Pair(quizMasterReference.get().participants[1].id, "Im Fuß")
                                ))
                                .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                                .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
                }
                .hasOpenQuestion(1) { openQuestion ->
                        openQuestion
                                .hasQuestion("Was ist ein Robo-Advisor?")
                                .hasAnswerNote("Ein algorithmen gesteuertes Dings")
                                .isNotPending
                                .isEstimationQuestion
                }
                .playedQuestionSizeIs(1)
                .hasPlayedQuestion(0) { playedQuestion ->
                        playedQuestion
                                .hasQuestion("Wer schrieb das Buch Animal Farm?")
                                .hasAnswerNote("George Orwell")
                                .isNotPending
                }
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").hasPoints(0).allowsReveal().isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").hasPoints(2).allowsReveal().isNotTurn }
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        Thread.sleep(10)

        // Participant 2 changes the answer
        webTestClient
                .put()
                .uri(quizMasterReference.get().openQuestions[0].choices!![1].getLink("${quizMasterReference.get().participants[1].id}-selects-choice").map { it.href }.orElseThrow())
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(2)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isPending
                            .isMultipleChoiceQuestion
                            .hasEstimates(mapOf(
                                    Pair(quizMasterReference.get().participants[0].id, "Im Fuß"),
                                    Pair(quizMasterReference.get().participants[1].id, "In der Hand")
                            ))
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
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
                .hasParticipant(0) { it.hasName("André").hasPoints(0).allowsReveal().isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").hasPoints(2).allowsReveal().isNotTurn }
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        Thread.sleep(10)

        // Answers are revealed
        webTestClient
                .patch()
                .uri(quizMasterReference.get().getLink("revealAnswers").map { it.href }.orElseThrow())
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(2)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isPending
                            .isMultipleChoiceQuestion
                            .hasEstimates(mapOf(
                                    Pair(quizMasterReference.get().participants[0].id, "Im Fuß"),
                                    Pair(quizMasterReference.get().participants[1].id, "In der Hand")
                            ))
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
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
                .hasParticipant(0) { it.hasName("André").hasPoints(0).allowsReveal().isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").hasPoints(2).allowsReveal().isNotTurn }
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        Thread.sleep(10)

        // Answer of participant 2 was correct
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
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isPending
                            .isMultipleChoiceQuestion
                            .hasEstimates(mapOf(
                                    Pair(quizMasterReference.get().participants[0].id, "Im Fuß"),
                                    Pair(quizMasterReference.get().participants[1].id, "In der Hand")
                            ))
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .hasAnswerNote("Ein algorithmen gesteuertes Dings")
                            .isNotPending
                            .isEstimationQuestion
                }
                .playedQuestionSizeIs(1)
                .hasPlayedQuestion(0) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .hasAnswerNote("George Orwell")
                            .isNotPending
                }
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").hasPoints(0).allowsReveal().isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").hasPoints(6).allowsReveal().isNotTurn }
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        Thread.sleep(10)

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
                            .isMultipleChoiceQuestion
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
                }
                .hasOpenQuestion(1) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .hasAnswerNote("Ein algorithmen gesteuertes Dings")
                            .isNotPending
                            .isEstimationQuestion
                }
                .playedQuestionSizeIs(1)
                .hasPlayedQuestion(0) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .hasAnswerNote("George Orwell")
                            .isNotPending
                }
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").hasPoints(0).allowsReveal().isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").hasPoints(6).allowsReveal().isNotTurn }
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        Thread.sleep(10)

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
                            .hasAnswerNote("Ein algorithmen gesteuertes Dings")
                            .isPending
                            .isEstimationQuestion
                }
                .playedQuestionSizeIs(2)
                .hasPlayedQuestion(0) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .hasAnswerNote("George Orwell")
                            .isNotPending
                }
                .hasPlayedQuestion(1) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isMultipleChoiceQuestion
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
                }
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").hasPoints(0).allowsReveal().isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").hasPoints(6).allowsReveal().isNotTurn }
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        Thread.sleep(10)

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
                            .hasAnswerNote("Ein algorithmen gesteuertes Dings")
                            .isPending
                            .isEstimationQuestion
                            .hasEstimates(mapOf(Pair(quizMasterReference.get().participants[0].id, "Antwort von André")))
                }
                .playedQuestionSizeIs(2)
                .hasPlayedQuestion(0) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .hasAnswerNote("George Orwell")
                            .isNotPending
                }
                .hasPlayedQuestion(1) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isMultipleChoiceQuestion
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
                }
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").hasPoints(0).allowsReveal().isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").hasPoints(6).allowsReveal().isNotTurn }
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        Thread.sleep(10)

        // Participant 2 does not want the answer to be revealed
        webTestClient
                .put()
                .uri(quizMasterReference.get().participants[1].getLink("toggleRevealAllowed").map { it.href }.orElseThrow())
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(1)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .hasAnswerNote("Ein algorithmen gesteuertes Dings")
                            .isPending
                            .isEstimationQuestion
                            .hasEstimates(java.util.Map.of(quizMasterReference.get().participants[0].id, "Antwort von André"))
                }
                .playedQuestionSizeIs(2)
                .hasPlayedQuestion(0) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .hasAnswerNote("George Orwell")
                            .isNotPending
                }
                .hasPlayedQuestion(1) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isMultipleChoiceQuestion
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
                }
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").hasPoints(0).allowsReveal().isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").hasPoints(6).doesNotAllowReveal().isNotTurn }
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        Thread.sleep(10)

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
                            .hasAnswerNote("Ein algorithmen gesteuertes Dings")
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
                            .hasAnswerNote("George Orwell")
                            .isNotPending
                }
                .hasPlayedQuestion(1) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isMultipleChoiceQuestion
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
                }
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").hasPoints(0).allowsReveal().isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").hasPoints(6).doesNotAllowReveal().isNotTurn }
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        Thread.sleep(10)

        // Answers are revealed
        webTestClient
                .patch()
                .uri(quizMasterReference.get().getLink("revealAnswers").map { it.href }.orElseThrow())
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(1)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .hasAnswerNote("Ein algorithmen gesteuertes Dings")
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
                            .hasAnswerNote("George Orwell")
                            .isNotPending
                }
                .hasPlayedQuestion(1) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isMultipleChoiceQuestion
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
                }
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").hasPoints(0).allowsReveal().isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").hasPoints(6).doesNotAllowReveal().isNotTurn }
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        Thread.sleep(10)

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
                            .hasAnswerNote("Ein algorithmen gesteuertes Dings")
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
                            .hasAnswerNote("George Orwell")
                            .isNotPending
                }
                .hasPlayedQuestion(1) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isMultipleChoiceQuestion
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
                }
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").hasPoints(2).allowsReveal().isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").hasPoints(6).doesNotAllowReveal().isNotTurn }
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        Thread.sleep(10)

        // Participant 1 is deleted
        webTestClient
                .delete()
                .uri(quizMasterReference.get().participants[0].getLink("delete").map { it.href }.orElseThrow())
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(1)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .hasAnswerNote("Ein algorithmen gesteuertes Dings")
                            .isPending
                            .isEstimationQuestion
                            .hasEstimates(java.util.Map.of(quizMasterReference.get().participants[0].id, "Antwort von Lena"))
                }
                .playedQuestionSizeIs(2)
                .hasPlayedQuestion(0) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .hasAnswerNote("George Orwell")
                            .isNotPending
                }
                .hasPlayedQuestion(1) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isMultipleChoiceQuestion
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
                }
                .particpantSizeIs(1)
                .hasParticipant(0) { it.hasName("Lena").hasPoints(6).doesNotAllowReveal().isNotTurn }
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        Thread.sleep(10)

        // Quiz master performs an undo
        webTestClient
                .delete()
                .uri(quizMasterReference.get().getLink("undo").map { it.href }.orElseThrow())
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(1)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .hasAnswerNote("Ein algorithmen gesteuertes Dings")
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
                            .hasAnswerNote("George Orwell")
                            .isNotPending
                }
                .hasPlayedQuestion(1) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isMultipleChoiceQuestion
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
                }
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").hasPoints(2).allowsReveal().isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").hasPoints(6).doesNotAllowReveal().isNotTurn }
                .undoIsPossible()
                .redoIsPossible()
                .isNotFinished

        Thread.sleep(10)

        // Quiz master performs a redo
        webTestClient
                .post()
                .uri(quizMasterReference.get().getLink("redo").map { it.href }.orElseThrow())
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(1)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .hasAnswerNote("Ein algorithmen gesteuertes Dings")
                            .isPending
                            .isEstimationQuestion
                            .hasEstimates(java.util.Map.of(quizMasterReference.get().participants[0].id, "Antwort von Lena"))
                }
                .playedQuestionSizeIs(2)
                .hasPlayedQuestion(0) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .hasAnswerNote("George Orwell")
                            .isNotPending
                }
                .hasPlayedQuestion(1) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isMultipleChoiceQuestion
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
                }
                .particpantSizeIs(1)
                .hasParticipant(0) { it.hasName("Lena").hasPoints(6).doesNotAllowReveal().isNotTurn }
                .undoIsPossible()
                .redoIsNotPossible()
                .isNotFinished

        // Quiz master performs an undo
        webTestClient
                .delete()
                .uri(quizMasterReference.get().getLink("undo").map { it.href }.orElseThrow())
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(1)
                .hasOpenQuestion(0) { openQuestion ->
                    openQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .hasAnswerNote("Ein algorithmen gesteuertes Dings")
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
                            .hasAnswerNote("George Orwell")
                            .isNotPending
                }
                .hasPlayedQuestion(1) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isMultipleChoiceQuestion
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
                }
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").hasPoints(2).allowsReveal().isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").hasPoints(6).doesNotAllowReveal().isNotTurn }
                .undoIsPossible()
                .redoIsPossible()
                .isNotFinished

        Thread.sleep(10)

        // Finish the quiz

        webTestClient
                .post()
                .uri(quizMasterReference.get().getLink("finish").map { it.href }.orElseThrow())
                .exchange()
                .expectStatus().isOk

        assertThat(quizMasterReference.get())
                .openQuestionSizeIs(0)
                .playedQuestionSizeIs(3)
                .hasPlayedQuestion(0) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wer schrieb das Buch Animal Farm?")
                            .hasAnswerNote("George Orwell")
                            .isNotPending
                }
                .hasPlayedQuestion(1) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Wo befindet sich das Kahnbein?")
                            .isNotPending
                            .isMultipleChoiceQuestion
                            .hasChoice(0) { choice -> choice.hasChoice("Im Fuß") }
                            .hasChoice(1) { choice -> choice.hasChoice("In der Hand") }
                }
                .hasPlayedQuestion(2) { playedQuestion ->
                    playedQuestion
                            .hasQuestion("Was ist ein Robo-Advisor?")
                            .hasAnswerNote("Ein algorithmen gesteuertes Dings")
                            .isPending
                            .isEstimationQuestion
                            .hasEstimates(java.util.Map.of(
                                    quizMasterReference.get().participants[0].id, "Antwort von André",
                                    quizMasterReference.get().participants[1].id, "Antwort von Lena"
                            ))
                }
                .particpantSizeIs(2)
                .hasParticipant(0) { it.hasName("André").hasPoints(2).allowsReveal().isNotTurn }
                .hasParticipant(1) { it.hasName("Lena").hasPoints(6).doesNotAllowReveal().isNotTurn }
                .undoIsPossible()
                .redoIsNotPossible()
                .hasQuizStatistics { quizStatistics ->
                    quizStatistics
                            .participantStatisticsSizeIs(2)
                }
                .isFinished
    }
}
