package team.undefined.quiz.web

import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder
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
                .map { it.map() }
    }

    @GetMapping("/{quizId}", produces = ["application/json"])
    fun determineQuiz(@PathVariable quizId: Long): Mono<QuizDTO> {
        return quizService.determineQuiz(quizId)
                .map { it.map() }
    }
/*
    private fun addLinks(quiz: QuizDTO): Mono<QuizDTO> {
        linkTo(methodOn(QuizController::class.java).determineQuiz(quiz.id!!))
                .withSelfRel()
                .toMono()
                .map { quiz.add(it) }
                .map { linkTo(methodOn(ParticipantsController::class.java)) }
    }
    */

}

fun Quiz.map(): QuizDTO {
    return QuizDTO(this.id, this.name, this.participants, this.turn)
}

private fun QuizDTO.map(): Quiz {
    return Quiz(null, this.name)
}
