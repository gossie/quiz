package team.undefined.quiz.web

import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import team.undefined.quiz.core.Quiz
import team.undefined.quiz.core.QuizService

@RestController
@CrossOrigin(origins = ["*"], allowedHeaders = ["*"])
@RequestMapping("/api/quiz")
class QuizController(private val quizService: QuizService) {

    @PostMapping(produces = ["application/json"])
    @ResponseStatus(HttpStatus.CREATED)
    fun createQuiz(@RequestBody quiz: QuizDTO): Mono<QuizDTO> {
        return quizService.createQuiz(quiz.map())
                .flatMap { it.map() }
    }

    @GetMapping("/{quizId}", produces = ["application/json"])
    fun determineQuiz(@PathVariable quizId: Long): Mono<QuizDTO> {
        return quizService.determineQuiz(quizId)
                .flatMap { it.map() }
    }

}

fun Quiz.map(): Mono<QuizDTO> {
    return QuizDTO(this.id, this.name, this.participants.map { it.name }, this.questions, this.turn)
            .addLinks()
}

private fun QuizDTO.map(): Quiz {
    return Quiz(null, this.name)
}

private fun QuizDTO.addLinks(): Mono<QuizDTO> {
    return linkTo(methodOn(ParticipantsController::class.java).create(this.id!!, ""))
            .withRel("createParticipant")
            .toMono()
            .map { this.add(it) }
            .map { linkTo(methodOn(QuestionController::class.java).createQuestion(this.id!!, "")) }
            .map { it.withRel("createQuestion") }
            .flatMap { it.toMono() }
            .map { this.add(it) }
}
