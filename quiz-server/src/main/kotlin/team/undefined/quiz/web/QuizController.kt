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
    fun createQuiz(@RequestBody quizDTO: QuizDTO): Mono<QuizDTO> {
        val quiz = quizDTO.map()
        return quizService.createQuiz(CreateQuizCommand(quiz.id, quiz))
                .map { quizProjection.determineQuiz(quiz.id) }
                .flatMap { it.map() }
    }

    @PatchMapping("/{quizId}", consumes = ["text/plain"], produces = ["application/json"])
    fun answer(@PathVariable quizId: UUID, @RequestBody correct: String): Mono<QuizDTO> {
        return if (correct == "true") {
            quizService.answer(AnswerCommand(quizId, AnswerCommand.Answer.CORRECT))
                    .map { quizProjection.determineQuiz(quizId) }
                    .flatMap { it.map() }
        } else {
            quizService.answer(AnswerCommand(quizId, AnswerCommand.Answer.INCORRECT))
                    .map { quizProjection.determineQuiz(quizId) }
                    .flatMap { it.map() }
        }
    }

    @PutMapping("/{quizId}", produces = ["application/json"])
    fun reopenQuestion(@PathVariable quizId: UUID): Mono<QuizDTO> {
        return quizService.reopenQuestion(ReopenCurrentQuestionCommand(quizId))
                .map { quizProjection.determineQuiz(quizId) }
                .flatMap { it.map() }
    }

    @GetMapping("/{quizId}", produces = ["application/json"])
    fun determineQuiz(@PathVariable quizId: UUID): Mono<QuizDTO> {
        return Mono.just(quizProjection.determineQuiz(quizId))
                .flatMap { it.map() }
    }

}

fun Quiz.map(): Mono<QuizDTO> {
    return Flux.fromIterable(this.participants)
            .flatMap { it.map(this.id) }
            .collect(Collectors.toList())
            .map { QuizDTO(this.id, this.name, it, this.questions.filter { it.alreadyPlayed }.map { it.map(this.id) }, this.questions.filter { !it.alreadyPlayed }.map { it.map(this.id) }) }
            .flatMap { it.addLinks() }
}

private fun Participant.map(quizId: UUID): Mono<ParticipantDTO> {
    return ParticipantDTO(this.id, this.name, this.turn, this.points)
            .addLinks(quizId)
}

private fun ParticipantDTO.addLinks(quizId: UUID): Mono<ParticipantDTO> {
    return linkTo(methodOn(ParticipantsController::class.java).buzzer(quizId, this.id))
            .withRel("buzzer")
            .toMono()
            .map { this.add(it) }
}

private fun Question.map(quizId: UUID): QuestionDTO {
    val questionDTO = QuestionDTO(this.id, this.question, this.pending, this.imageUrl)
    questionDTO.add(Link("/api/quiz/" + quizId + "/questions/" + this.id, "self"))
    return if (this.imageUrl == "") questionDTO else questionDTO.add(Link(this.imageUrl, "image"))
}
/*
private fun QuestionDTO.addLinks(quizId: UUID): Mono<QuestionDTO> {
    return linkTo(methodOn(QuestionController::class.java).startQuestion(quizId, this.id))
            .withSelfRel()
            .toMono()
            .map { this.add(it) }
}
*/
private fun QuizDTO.map(): Quiz {
    return Quiz(name = this.name)
}

private fun QuizDTO.addLinks(): Mono<QuizDTO> {
    return linkTo(methodOn(ParticipantsController::class.java).create(this.id!!, ""))
            .withRel("createParticipant")
            .toMono()
            .map { this.add(it) }
            .map { linkTo(methodOn(QuestionController::class.java).createQuestion(this.id!!, null)) }
            .map { it.withRel("createQuestion") }
            .flatMap { it.toMono() }
            .map { this.add(it) }
            .map { linkTo(methodOn(QuizController::class.java).answer(this.id!!, "")) }
            .map { it.withRel("answer") }
            .flatMap { it.toMono() }
            .map { this.add(it) }
            .map { linkTo(methodOn(QuizController::class.java).reopenQuestion(this.id!!)) }
            .map { it.withRel("reopenQuestion") }
            .flatMap { it.toMono() }
            .map { this.add(it) }
}
