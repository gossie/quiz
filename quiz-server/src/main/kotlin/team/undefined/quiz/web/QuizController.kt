package team.undefined.quiz.web

import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import team.undefined.quiz.core.Participant
import team.undefined.quiz.core.Quiz
import team.undefined.quiz.core.QuizService
import java.util.stream.Collectors

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

    @PatchMapping("/{quizId}", consumes = ["text/plain"], produces = ["application/json"])
    fun answer(@PathVariable quizId: Long, @RequestBody correct: String): Mono<QuizDTO> {
        return if (correct == "true") {
            quizService.correctAnswer(quizId)
                    .flatMap { it.map() }
        } else {
            quizService.incorrectAnswer(quizId)
                    .flatMap { it.map() }
        }
    }

    @GetMapping("/{quizId}", produces = ["application/json"])
    fun determineQuiz(@PathVariable quizId: Long): Mono<QuizDTO> {
        return quizService.determineQuiz(quizId)
                .flatMap { it.map() }
    }

}

fun Quiz.map(): Mono<QuizDTO> {
    return Flux.fromIterable(this.participants)
            .flatMap { it.map(this.id!!) }
            .collect(Collectors.toList())
            .map { QuizDTO(this.id, this.name, it, this.questions.map { it.question }) }
            .flatMap { it.addLinks() }
}

private fun Participant.map(quizId: Long): Mono<ParticipantDTO> {
    return ParticipantDTO(this.id!!, this.name, this.turn, this.points)
            .addLinks(quizId)
}

private fun ParticipantDTO.addLinks(quizId: Long): Mono<ParticipantDTO> {
    return linkTo(methodOn(ParticipantsController::class.java).buzzer(quizId, this.id))
            .withRel("buzzer")
            .toMono()
            .map { this.add(it) }
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
            .map { linkTo(methodOn(QuizController::class.java).answer(this.id!!, "")) }
            .map { it.withRel("answer") }
            .flatMap { it.toMono() }
            .map { this.add(it) }
}
