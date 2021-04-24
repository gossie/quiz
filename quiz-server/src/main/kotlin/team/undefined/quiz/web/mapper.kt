package team.undefined.quiz.web

import org.springframework.hateoas.Link
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import team.undefined.quiz.core.*
import java.util.*
import java.util.stream.Collectors

fun Quiz.map(quizStatistics: QuizStatistics? = null): Mono<QuizDTO> {
    return Flux.fromIterable(this.participants)
            .flatMap { it.map(this.id) }
            .collect(Collectors.toList())
            .map { participants ->
                val quizDTO = QuizDTO(this.id, this.name, participants, this.questions.filter { it.alreadyPlayed }.map { it.map(this) }, this.questions.filter { !it.alreadyPlayed }.map { it.map(this) }, this.undoPossible, this.redoPossible, this.finished, timestamp = this.timestamp, expirationDate = this.timestamp + 2_419_200_000, points = this.points)
                quizDTO.quizStatistics = quizStatistics?.map(quizDTO)
                quizDTO
            }
            .flatMap { it.addLinks() }
}

private fun QuizStatistics.map(quiz: QuizDTO): QuizStatisticsDTO {
    return QuizStatisticsDTO(this.participantStatistics.map { it.map(quiz) })
}

private fun ParticipantStatistics.map(quiz: QuizDTO): ParticipantStatisticsDTO {
    return ParticipantStatisticsDTO(
            quiz.participants.find { it.id == this.id },
            this.questionStatistics.map { it.map(quiz) }
    )
}

private fun QuestionStatistics.map(quiz: QuizDTO): QuestionStatisticsDTO {
    return QuestionStatisticsDTO(
            quiz.playedQuestions.find { it.id == this.id }!!,
            this.ratings
    )
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

fun Question.map(quiz: Quiz): QuestionDTO {
    val questionDTO = QuestionDTO(
            this.id,
            this.question,
            this.pending,
            this.imageUrl,
            if (this.estimates != null) { HashMap(this.estimates) } else { this.estimates },
            this.visibility.asBoolean(),
            this.category.category,
            this.initialTimeToAnswer,
            this.secondsLeft,
            this.revealed,
            this.previousQuestionId,
            if (this.choices != null) { this.choices.map { it.map(quiz) } } else { null },
            this.correctAnswer,
            this.points
    )
    questionDTO.add(Link.of("/api/quiz/" + quiz.id + "/questions/" + this.id, "self"))
    return if (this.imageUrl == "") questionDTO else questionDTO.add(Link.of(this.imageUrl, "image"))
}

fun Question.map(quizId: UUID): QuestionDTO {
    val questionDTO = QuestionDTO(
            this.id,
            this.question,
            this.pending,
            this.imageUrl,
            if (this.estimates != null) { HashMap(this.estimates) } else { this.estimates },
            this.visibility.asBoolean(),
            this.category.category,
            this.initialTimeToAnswer,
            this.secondsLeft,
            this.revealed,
            this.previousQuestionId,
            if (this.choices != null) { this.choices.map { it.map() } } else { null },
            this.correctAnswer,
            this.points
    )
    questionDTO.add(Link.of("/api/quiz/" + quizId + "/questions/" + this.id, "self"))
    return if (this.imageUrl == "") questionDTO else questionDTO.add(Link.of(this.imageUrl, "image"))
}

fun Choice.map(quiz: Quiz): ChoiceDTO {
    val choiceDTO = ChoiceDTO(this.id, this.choice)
    quiz.participants.forEach {
        choiceDTO.add(Link.of("/api/quiz/" + quiz.id + "/participants/" + it.id + "/choices/" + this.id, "${it.id}-selects-choice"))
    }
    return choiceDTO
}

fun Choice.map(): ChoiceDTO {
    return ChoiceDTO(this.id, this.choice)
}

fun QuestionDTO.map(questionId: UUID): Question {
    return Question(
            questionId,
            question = this.question,
            imageUrl = this.imagePath,
            visibility = if (this.publicVisible) Question.QuestionVisibility.PUBLIC else Question.QuestionVisibility.PRIVATE,
            category = if (this.category == "") QuestionCategory("other") else QuestionCategory(this.category),
            initialTimeToAnswer = this.timeToAnswer,
            secondsLeft = this.timeToAnswer,
            revealed = this.revealed,
            previousQuestionId = this.previousQuestionId,
            choices = this.choices?.map { it.map() },
            estimates = this.estimates,
            correctAnswer = this.correctAnswer,
            points = this.points
    )
}

fun QuestionDTO.map(): Question {
    return Question(
            question = this.question,
            imageUrl = this.imagePath,
            visibility = if (this.publicVisible) Question.QuestionVisibility.PUBLIC else Question.QuestionVisibility.PRIVATE,
            category = if (this.category == "") QuestionCategory("other") else QuestionCategory(this.category),
            initialTimeToAnswer = this.timeToAnswer,
            secondsLeft = this.timeToAnswer,
            previousQuestionId = this.previousQuestionId,
            choices = this.choices?.map { it.map() },
            estimates = this.estimates,
            correctAnswer = this.correctAnswer,
            points = this.points
    )
}

fun ChoiceDTO.map(): Choice {
    return Choice(choice = this.choice)
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
