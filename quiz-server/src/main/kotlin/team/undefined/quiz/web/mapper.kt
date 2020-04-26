package team.undefined.quiz.web

import org.springframework.hateoas.Link
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import team.undefined.quiz.core.Participant
import team.undefined.quiz.core.Question
import team.undefined.quiz.core.Quiz
import java.util.*
import java.util.stream.Collectors

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

private fun QuestionDTO.addLinks(quizId: UUID): Mono<QuestionDTO> {
    return linkTo(methodOn(QuestionController::class.java).startQuestion(quizId, this.id!!))
            .withSelfRel()
            .toMono()
            .map { this.add(it) }
}

fun QuizDTO.map(): Quiz {
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
