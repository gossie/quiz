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
            .flatMap { participants ->
                val quizDTO = QuizDTO(this.id, this.name, participants, this.questions.filter { it.alreadyPlayed }.map { it.map(this.id) }, this.questions.filter { !it.alreadyPlayed }.map { it.map(this.id) }, this.isUndoPossible(), this.isRedoPossible(), this.finished, timestamp = this.getTimestamp())
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
    return Flux.concat(this.answerStatistics.map { it.map(quiz) })
            .collect(Collectors.toList())
            .map { buzzerStatistics ->
                val q = quiz.questions.find { it.id == this.questionId }
                val mapped = q?.map(quiz.id)
                QuestionStatisticsDTO(
                        mapped!!,
                        buzzerStatistics
                )
            }
            .onErrorMap {
                it.printStackTrace()
                it
            }
}

private fun AnswerStatistics.map(quiz: Quiz): Mono<AnswerStatisticsDTO> {
    return quiz.participants.find { it.id == this.participantId }!!.map(quiz.id)
            .map {
                AnswerStatisticsDTO(
                        it,
                        this.duration,
                        this.answer,
                        this.rating
                )
            }
}

private fun Participant.map(quizId: UUID): Mono<ParticipantDTO> {
    return ParticipantDTO(this.id, this.name, this.turn, this.points, this.revealAllowed)
            .addLinks(quizId)
}

private fun ParticipantDTO.addLinks(quizId: UUID): Mono<ParticipantDTO> {
    return linkTo(methodOn(ParticipantsController::class.java).buzzer(quizId, this.id, ""))
            .withRel("buzzer")
            .toMono()
            .map { this.add(it) }
            .map { linkTo(methodOn(ParticipantsController::class.java).toggleRevealPrevention(quizId, this.id)) }
            .map { it.withRel("toggleRevealAllowed") }
            .flatMap { it.toMono() }
            .map { this.add(it) }
            .map { linkTo(methodOn(ParticipantsController::class.java).delete(quizId, this.id)) }
            .map { it.withRel("delete") }
            .flatMap { it.toMono() }
            .map { this.add(it) }
}

fun Question.map(quizId: UUID): QuestionDTO {
    val questionDTO = QuestionDTO(this.id, this.question, this.pending, this.imageUrl, if (this.estimates != null) { HashMap(this.estimates) } else { this.estimates }, this.visibility.asBoolean(), this.category.category, this.initialTimeToAnswer, this.secondsLeft, this.revealed)
    questionDTO.add(Link.of("/api/quiz/" + quizId + "/questions/" + this.id, "self"))
    return if (this.imageUrl == "") questionDTO else questionDTO.add(Link.of(this.imageUrl, "image"))
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
    var linkBuilder = linkTo(methodOn(ParticipantsController::class.java).create(this.id!!, "", null))
            .withRel("createParticipant")
            .toMono()
            .map { this.add(it) }
            .map { linkTo(methodOn(QuestionController::class.java).createQuestion(this.id!!, null)) }
            .map { it.withRel("createQuestion") }
            .flatMap { it.toMono() }
            .map { this.add(it) }
            .map { linkTo(methodOn(QuizController::class.java).revealAnswers(this.id!!)) }
            .map { it.withRel("revealAnswers") }
            .flatMap { it.toMono() }
            .map { this.add(it) }
            .map { linkTo(methodOn(QuizController::class.java).undo(this.id!!)) }
            .map { it.withRel("undo") }
            .flatMap { it.toMono() }
            .map { this.add(it) }
            .map { linkTo(methodOn(QuizController::class.java).redo(this.id!!)) }
            .map { it.withRel("redo") }
            .flatMap { it.toMono() }
            .map { this.add(it) }

    this.participants.forEach { participant ->
        linkBuilder = linkBuilder
                .map { linkTo(methodOn(QuizController::class.java).answer(this.id!!, participant.id, "")) }
                .map { it.withRel("answer-${participant.id}") }
                .flatMap { it.toMono() }
                .map { this.add(it) }
    }

    return linkBuilder
            .map { linkTo(methodOn(QuizController::class.java).reopenCurrentQuestion(this.id!!)) }
            .map { it.withRel("reopenQuestion") }
            .flatMap { it.toMono() }
            .map { this.add(it) }
            .map { linkTo(methodOn(QuizController::class.java).finish(this.id!!)) }
            .map { it.withRel("finish") }
            .flatMap { it.toMono() }
            .map { this.add(it) }
}
