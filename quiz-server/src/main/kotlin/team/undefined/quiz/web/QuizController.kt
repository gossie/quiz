package team.undefined.quiz.web

import com.google.common.eventbus.EventBus
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import team.undefined.quiz.core.*
import java.time.Duration
import java.util.*


@RestController
@CrossOrigin(origins = ["*"], allowedHeaders = ["*"])
@RequestMapping("/api/quiz")
class QuizController(private val quizService: QuizService,
                     private val quizProjection: QuizProjection,
                     private val eventBus: EventBus) {

    private val logger = LoggerFactory.getLogger(QuizController::class.java)

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.TEXT_PLAIN_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun createQuiz(@RequestBody quizDTO: QuizDTO): Mono<String> {
        val quiz = quizDTO.map()
        return quizService.createQuiz(CreateQuizCommand(quiz.id, quiz))
                .map { quiz.id.toString() }
    }

    @PostMapping("/{quizId}")
    @ResponseStatus(HttpStatus.OK)
    fun finish(@PathVariable quizId: UUID): Mono<Unit> {
        return quizService.finishQuiz(FinishQuizCommand(quizId))
    }

    @PostMapping("/{quizId}/participants/{participantId}/answers", consumes = [MediaType.TEXT_PLAIN_VALUE])
    @ResponseStatus(HttpStatus.OK)
    fun answer(@PathVariable quizId: UUID, @PathVariable participantId: UUID, @RequestBody correct: String): Mono<Unit> {
        return if (correct == "true") {
            quizService.answer(AnswerCommand(quizId, participantId, AnswerCommand.Answer.CORRECT))
        } else {
            quizService.answer(AnswerCommand(quizId, participantId, AnswerCommand.Answer.INCORRECT))
        }
    }

    @PutMapping("/{quizId}")
    @ResponseStatus(HttpStatus.OK)
    fun reopenCurrentQuestion(@PathVariable quizId: UUID): Mono<Unit> {
        return quizService.reopenQuestion(ReopenCurrentQuestionCommand(quizId));
    }

    @PatchMapping("/{quizId}")
    @ResponseStatus(HttpStatus.OK)
    fun revealAnswers(@PathVariable quizId: UUID): Mono<Unit> {
        return quizService.revealAnswers(RevealAnswersCommand(quizId));
    }

    @GetMapping(path = ["/{quizId}/quiz-master"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getQuizStreamForQuizMaster(@PathVariable quizId: UUID): Flux<ServerSentEvent<QuizDTO>> {
        val observer = getObserver(quizId)
        eventBus.post(ForceEmitCommand(quizId))
        return Flux.merge(observer, getHeartbeat(quizId))
    }

    @GetMapping(path = ["/{quizId}/quiz-participant"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getQuizStreamForQuizParticipants(@PathVariable quizId: UUID): Flux<ServerSentEvent<QuizDTO>> {
        val observer = getObserver(quizId)
        eventBus.post(ForceEmitCommand(quizId))
        return Flux.merge(observer, getHeartbeat(quizId))
                .map {
                    it.data()?.openQuestions
                        ?.filter { question -> !question.revealed }
                        ?.forEach { question ->
                            question.estimates?.keys?.forEach { id ->
                                (question.estimates as MutableMap)[id] = "*****";
                            }
                    }
                    it
                }
    }

    private fun getObserver(quizId: UUID): Flux<ServerSentEvent<QuizDTO>> {
        return quizProjection.observeQuiz(quizId)
                .flatMap { it.map() }
                .map {
                    logger.info("Sending quiz {} to the client", it)
                    ServerSentEvent.builder<QuizDTO>()
                            .event("quiz")
                            .data(it)
                            .build()
                }
    }

    private fun getHeartbeat(quizId: UUID): Flux<ServerSentEvent<QuizDTO>> {
        return Flux.interval(Duration.ofSeconds(10))
                .map { quizProjection.determineQuiz(quizId) }
                .filter { it != null }
                .flatMap { it!!.map() }
                .map {
                    logger.info("Sending quiz {} to the client as heartbeat", it)
                    ServerSentEvent.builder<QuizDTO>()
                            .event("quiz")
                            .data(it)
                            .build()
                }
    }

}