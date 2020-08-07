package team.undefined.quiz.web

import com.google.common.eventbus.EventBus
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
    fun reopenQuestion(@PathVariable quizId: UUID): Mono<Unit> {
        return quizService.reopenQuestion(ReopenCurrentQuestionCommand(quizId));
    }

    @GetMapping(path = ["/{quizId}/quiz-master"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getQuizStreamForQuizMaster(@PathVariable quizId: UUID): Flux<ServerSentEvent<QuizDTO>> {
        val observer = getObserver(quizId)
        eventBus.post(ForceEmitCommand(quizId))
        return Flux.merge(observer, getHeartbeat())
    }

    @GetMapping(path = ["/{quizId}/quiz-participant"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getQuizStreamForQuizParticipants(@PathVariable quizId: UUID): Flux<ServerSentEvent<QuizDTO>> {
        val observer = getObserver(quizId)
        eventBus.post(ForceEmitCommand(quizId))
        return Flux.merge(observer, getHeartbeat())
                .map {
                    it.data()?.openQuestions?.forEach { question ->
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
                    ServerSentEvent.builder<QuizDTO>()
                            .event("quiz")
                            .data(it)
                            .build()
                }
    }

    private fun getHeartbeat(): Flux<ServerSentEvent<QuizDTO>> {
        return Flux.interval(Duration.ofSeconds(10))
                .map {
                    ServerSentEvent.builder<QuizDTO>()
                            .event("ping")
                            .build()
                }
    }

}