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
            .flatMap { participants ->
                val quizDTO = QuizDTO(this.id, this.name, participants, this.questions.filter { it.alreadyPlayed }.map { it.map(this) }, this.questions.filter { !it.alreadyPlayed }.map { it.map(this) }, this.undoPossible, this.redoPossible, this.finished, timestamp = this.timestamp, expirationDate = this.timestamp + 2_419_200_000)
                quizStatistics?.map(this)?.map {
                    quizDTO.quizStatistics = it
                    quizDTO
                } ?: Mono.just(quizDTO)
            }
            .flatMap { it.addLinks() }
}

private fun QuizStatistics.map(quiz: Quiz): Mono<QuizStatisticsDTO> {
    return Flux.concat(this.questionStatistics.map { it.map(quiz) })
            .collect(Collectors.toList())
            .map { QuizStatisticsDTO(it) }
}

private fun QuestionStatistics.map(quiz: Quiz): Mono<QuestionStatisticsDTO> {
    return Flux.concat(this.answerStatistics.map { it.map(quiz, this) })
            .collect(Collectors.toList())
            .map { buzzerStatistics ->
                val q = quiz.questions.find { it.id == this.questionId }
                val mapped = q?.map(quiz)
                QuestionStatisticsDTO(
                        mapped!!,
                        buzzerStatistics
                )
            }
}

private fun AnswerStatistics.map(quiz: Quiz, questionStatistics: QuestionStatistics): Mono<AnswerStatisticsDTO> {
    val question = quiz.questions.find { it.id == questionStatistics.questionId }
    return quiz.participants.find { it.id == this.participantId }!!.map(quiz.id)
            .map { participant ->
                AnswerStatisticsDTO(
                        participant,
                        this.duration,
                        if (this.choiceId != null) { question?.choices?.find { it.id == this.choiceId }?.choice } else { this.answer },
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
            this.correctAnswer
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
            this.correctAnswer
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
            correctAnswer = this.correctAnswer
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
            correctAnswer = this.correctAnswer
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
