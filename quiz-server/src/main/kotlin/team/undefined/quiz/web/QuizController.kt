package team.undefined.quiz.web

import org.springframework.hateoas.Link
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import team.undefined.quiz.core.*
import java.util.*
import java.util.stream.Collectors

@RestController
@CrossOrigin(origins = ["*"], allowedHeaders = ["*"])
@RequestMapping("/api/quiz")
class QuizController(private val quizService: QuizService,
                     private val quizProjection: QuizProjection) {

    @PostMapping(produces = ["application/json"])
    @ResponseStatus(HttpStatus.CREATED)
    fun createQuiz(@RequestBody quizDTO: QuizDTO): Mono<Unit> {
        val quiz = quizDTO.map()
        return quizService.createQuiz(CreateQuizCommand(quiz.id, quiz));
    }

    @PatchMapping("/{quizId}", consumes = ["text/plain"], produces = ["application/json"])
    fun answer(@PathVariable quizId: UUID, @RequestBody correct: String): Mono<Unit> {
        return if (correct == "true") {
            quizService.answer(AnswerCommand(quizId, AnswerCommand.Answer.CORRECT))
        } else {
            quizService.answer(AnswerCommand(quizId, AnswerCommand.Answer.INCORRECT))
        }
    }

    @PutMapping("/{quizId}", produces = ["application/json"])
    fun reopenQuestion(@PathVariable quizId: UUID): Mono<Unit> {
        return quizService.reopenQuestion(ReopenCurrentQuestionCommand(quizId));
    }

}
