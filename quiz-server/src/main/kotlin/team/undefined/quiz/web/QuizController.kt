package team.undefined.quiz.web

import org.springframework.hateoas.Link
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import team.undefined.quiz.core.Participant
import team.undefined.quiz.core.Question
import team.undefined.quiz.core.Quiz
import team.undefined.quiz.core.QuizService
import java.util.stream.Collectors


@RestController
@CrossOrigin(origins = ["*"], allowedHeaders = ["*"])
@RequestMapping("/api/quiz")
class QuizController(private val quizService: QuizService) {

    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun createQuiz(@RequestBody quiz: QuizDTO): Mono<QuizDTO> {
        return quizService.createQuiz(quiz.map())
                .flatMap { it.map() }
    }

    @PatchMapping("/{quizId}", consumes = [MediaType.TEXT_PLAIN_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun answer(@PathVariable quizId: Long, @RequestBody correct: String): Mono<QuizDTO> {
        return if (correct == "true") {
            quizService.correctAnswer(quizId)
                    .flatMap { it.map() }
        } else {
            quizService.incorrectAnswer(quizId)
                    .flatMap { it.map() }
        }
    }

    @PutMapping("/{quizId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun reopenQuestion(@PathVariable quizId: Long): Mono<QuizDTO> {
        return quizService.reopenQuestion(quizId)
                .flatMap { it.map() }
    }

    @GetMapping("/{quizId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun determineQuiz(@PathVariable quizId: Long): Mono<QuizDTO> {
        return quizService.determineQuiz(quizId)
                .flatMap { it.map() }
    }

    @GetMapping(path = ["/{quizId}/stream"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getQuizStream(@PathVariable quizId: Long): Flux<ServerSentEvent<QuizDTO>> {
        return quizService.observeQuiz(quizId)
                .flatMap { it.map() }
                .map {
                    ServerSentEvent.builder<QuizDTO>()
                            .event("quiz")
                            .data(it)
                            .build()
                }
    }

}

fun Quiz.map(): Mono<QuizDTO> {
    return Flux.fromIterable(this.participants)
            .flatMap { it.map(this.id!!) }
            .collect(Collectors.toList())
            .map { QuizDTO(this.id, this.name, it, this.questions.filter { it.alreadyPlayed }.map { it.map(this.id!!) }, this.questions.filter { !it.alreadyPlayed }.map { it.map(this.id!!) }) }
            .flatMap { it.addLinks() }
}

private fun Participant.map(quizId: Long): Mono<ParticipantDTO> {
    return ParticipantDTO(this.id!!, this.name, this.turn, this.points)
            .addLinks(quizId)
}

private fun ParticipantDTO.addLinks(quizId: Long): Mono<ParticipantDTO> {
    return linkTo(methodOn(ParticipantsController::class.java).buzzer(quizId, this.id!!))
            .withRel("buzzer")
            .toMono()
            .map { this.add(it) }
}

private fun Question.map(quizId: Long): QuestionDTO {
    val questionDTO = QuestionDTO(this.id, this.question, this.pending, this.imagePath)
    questionDTO.add(Link("/api/quiz/" + quizId + "/questions/" + this.id, "self"))
    return if (this.imagePath == "") questionDTO else questionDTO.add(Link(this.imagePath, "image"))
}
/*
private fun QuestionDTO.addLinks(quizId: Long): Mono<QuestionDTO> {
    return linkTo(methodOn(QuestionController::class.java).startQuestion(quizId, this.id!!))
            .withSelfRel()
            .toMono()
            .map { this.add(it) }
}
*/
private fun QuizDTO.map(): Quiz {
    return Quiz(null, this.name)
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
