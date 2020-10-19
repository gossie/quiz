package team.undefined.quiz.web

import org.springframework.hateoas.Link
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import team.undefined.quiz.core.*
import java.util.*
import java.util.stream.Collectors

fun Quiz.map(): Mono<QuizDTO> {
    return Flux.fromIterable(this.participants)
            .flatMap { it.map(this.id) }
            .collect(Collectors.toList())
            .flatMap {
                val quizDTO = QuizDTO(this.id, this.name, it, this.questions.filter { it.alreadyPlayed }.map { it.map(this.id) }, this.questions.filter { !it.alreadyPlayed }.map { it.map(this.id) }, this.finished, timestamp = this.getTimestamp())
                if (this.quizStatistics == null) {
                     Mono.just(quizDTO)
                } else {
                    this.quizStatistics!!
                            .map(this)
                            .map {
                                quizDTO.quizStatistics = it
                                quizDTO
                            }
                }
            }
            .flatMap { it.addLinks() }
}

private fun QuizStatistics.map(quiz: Quiz): Mono<QuizStatisticsDTO> {
    return Flux.concat(this.questionStatistics.map { it.map(quiz) })
            .collect(Collectors.toList())
            .map { QuizStatisticsDTO(it) }
}

private fun QuestionStatistics.map(quiz: Quiz): Mono<QuestionStatisticsDTO> {
    return Flux.concat(this.buzzerStatistics.map { it.map(quiz) })
            .collect(Collectors.toList())
            .map {
                QuestionStatisticsDTO(
                        quiz.questions.find { it.id == this.questionId }!!.map(quiz.id),
                        it
                )
            }
}

private fun BuzzerStatistics.map(quiz: Quiz): Mono<BuzzerStatisticsDTO> {
    return quiz.participants.find { it.id == this.participantId }!!.map(quiz.id)
            .map {
                BuzzerStatisticsDTO(
                        it,
                        this.duration,
                        this.answer
                )
            }
}

private fun Participant.map(quizId: UUID): Mono<ParticipantDTO> {
    return ParticipantDTO(this.id, this.name, this.turn, this.points)
            .addLinks(quizId)
}

private fun ParticipantDTO.addLinks(quizId: UUID): Mono<ParticipantDTO> {
    return linkTo(methodOn(ParticipantsController::class.java).buzzer(quizId, this.id, ""))
            .withRel("buzzer")
            .toMono()
            .map { this.add(it) }
}

fun Question.map(quizId: UUID): QuestionDTO {
    val questionDTO = QuestionDTO(this.id, this.question, this.pending, this.imageUrl, if (this.estimates != null) { HashMap(this.estimates) } else { this.estimates }, this.visibility.asBoolean(), this.category.category, this.initialTimeToAnswer, this.secondsLeft, this.revealed)
    questionDTO.add(Link("/api/quiz/" + quizId + "/questions/" + this.id, "self"))
    return if (this.imageUrl == "") questionDTO else questionDTO.add(Link(this.imageUrl, "image"))
}

private fun QuestionDTO.addLinks(quizId: UUID): Mono<QuestionDTO> {
    return linkTo(methodOn(QuestionController::class.java).startQuestion(quizId, this.id!!))
            .withSelfRel()
            .toMono()
            .map { this.add(it) }
}

fun QuestionDTO.map(questionId: UUID): Question {
    return Question(
            questionId,
            question = this.question,
            imageUrl = this.imagePath,
            estimates = this.estimates,
            visibility = if (this.publicVisible) Question.QuestionVisibility.PUBLIC else Question.QuestionVisibility.PRIVATE,
            category = if (this.category == "") QuestionCategory("other") else QuestionCategory(this.category),
            initialTimeToAnswer = this.timeToAnswer,
            secondsLeft = this.timeToAnswer,
            revealed = this.revealed
    )
}

fun QuestionDTO.map(): Question {
    return Question(
            question = this.question,
            imageUrl = this.imagePath,
            estimates = this.estimates,
            visibility = if (this.publicVisible) Question.QuestionVisibility.PUBLIC else Question.QuestionVisibility.PRIVATE,
            category = if (this.category == "") QuestionCategory("other") else QuestionCategory(this.category),
            initialTimeToAnswer = this.timeToAnswer,
            secondsLeft = this.timeToAnswer
    )
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
            .map { linkTo(methodOn(QuizController::class.java).answer(this.id!!, UUID.randomUUID(), "")) }
            .map { it.withRel("answer") }
            .flatMap { it.toMono() }
            .map { this.add(it) }
            .map { linkTo(methodOn(QuizController::class.java).reopenCurrentQuestion(this.id!!)) }
            .map { it.withRel("reopenQuestion") }
            .flatMap { it.toMono() }
            .map { this.add(it) }
}
