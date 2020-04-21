package team.undefined.quiz.web

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import team.undefined.quiz.core.*
import java.util.*


@RestController
@CrossOrigin(origins = ["*"], allowedHeaders = ["*"])
@RequestMapping("/api/quiz")
class QuizController(private val quizService: QuizService,
                     private val quizProjection: QuizProjection) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.TEXT_PLAIN_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun createQuiz(@RequestBody quizDTO: QuizDTO): Mono<String> {
        val quiz = quizDTO.map()
        return quizService.createQuiz(CreateQuizCommand(quiz.id, quiz))
                .map { quiz.id.toString() }
    }

    @PatchMapping("/{quizId}", consumes = [MediaType.TEXT_PLAIN_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    fun answer(@PathVariable quizId: UUID, @RequestBody correct: String): Mono<Unit> {
        return if (correct == "true") {
            quizService.answer(AnswerCommand(quizId, AnswerCommand.Answer.CORRECT))
        } else {
            quizService.answer(AnswerCommand(quizId, AnswerCommand.Answer.INCORRECT))
        }
    }

    @PutMapping("/{quizId}")
    @ResponseStatus(HttpStatus.OK)
    fun reopenQuestion(@PathVariable quizId: UUID): Mono<Unit> {
        return quizService.reopenQuestion(ReopenCurrentQuestionCommand(quizId));
    }

    @GetMapping(path = ["/{quizId}/stream"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getQuizStream(@PathVariable quizId: UUID): Flux<ServerSentEvent<QuizDTO>> {
        return quizProjection.observeQuiz(quizId)
                .flatMap { it.map() }
                .map {
                    ServerSentEvent.builder<QuizDTO>()
                            .event("quiz")
                            .data(it)
                            .build()
                }
    }

}