package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*
import kotlin.collections.HashMap

@Service
class DefaultQuizService(private val eventRepository: EventRepository,
                         private val undoneEventsCache: UndoneEventsCache,
                         private val eventBus: EventBus) : QuizService {

    private val logger = LoggerFactory.getLogger(QuizService::class.java)

    private val subscriptions = HashMap<UUID, Disposable>()

    @WriteLock
    override fun createQuiz(command: CreateQuizCommand): Mono<Unit> {
        logger.info("creating a new quiz")
        return eventRepository.storeEvent(QuizCreatedEvent(command.quizId, command.quiz))
                .map { eventBus.post(it) }
    }

    @WriteLock
    override fun createQuestion(command: CreateQuestionCommand): Mono<Unit> {
        logger.info("creating a new question for quiz '{}'", command.quizId)
        return eventRepository.determineEvents(command.quizId)
                .reduce(Quiz(name = "")) { quiz: Quiz, event: Event -> event.process(quiz)}
                .filter { !it.finished }
                .switchIfEmpty(Mono.error(QuizFinishedException()))
                .flatMap { eventRepository.storeEvent(QuestionCreatedEvent(
                    command.quizId,
                    command.question,
                    sequenceNumber = it.sequenceNumber + 1
                )) }
                .map {
                    undoneEventsCache.remove(it.quizId)
                    eventBus.post(it)
                }
    }

    @WriteLock
    override fun editQuestion(command: EditQuestionCommand): Mono<Unit> {
        logger.info("editing question '{}' of quiz '{}'", command.questionId, command.quizId)
        return eventRepository.determineEvents(command.quizId)
                .reduce(Quiz(name = "")) { quiz: Quiz, event: Event -> event.process(quiz)}
                .filter { !it.finished }
                .switchIfEmpty(Mono.error(QuizFinishedException()))
                .flatMap { eventRepository.storeEvent(QuestionEditedEvent(
                    command.quizId,
                    command.question,
                    sequenceNumber = it.sequenceNumber + 1
                )) }
                .map {
                    undoneEventsCache.remove(it.quizId)
                    eventBus.post(it)
                }
    }

    @WriteLock
    override fun createParticipant(command: CreateParticipantCommand): Mono<Unit> {
        logger.info("creating a new participant in quiz '{}'", command.quizId)
        eventBus.post(ForceEmitCommand(command.quizId))
        return eventRepository.determineEvents(command.quizId)
                .switchIfEmpty(Mono.error(QuizNotFoundException()))
                .reduce(Quiz(name = "")) { quiz: Quiz, event: Event -> event.process(quiz)}
                .filter { !it.finished }
                .switchIfEmpty(Mono.error(QuizFinishedException()))
                .filter { it.hasNoParticipantWithName(command.participant.name) }
                .flatMap { eventRepository.storeEvent(ParticipantCreatedEvent(
                    command.quizId,
                    command.participant,
                    sequenceNumber = it.sequenceNumber + 1
                )) }
                .map {
                    undoneEventsCache.remove(it.quizId)
                    eventBus.post(it)
                }
    }

    @WriteLock
    override fun deleteParticipant(command: DeleteParticipantCommand): Mono<Unit> {
        logger.info("deleting participant with id {} in quiz '{}'", command.participantId, command.quizId)
        return eventRepository.determineEvents(command.quizId)
                .reduce(Quiz(name = "")) { quiz: Quiz, event: Event -> event.process(quiz)}
                .filter { !it.finished }
                .switchIfEmpty(Mono.error(QuizFinishedException()))
                .filter { it.hasParticipantWithId(command.participantId) }
                .flatMap { eventRepository.storeEvent(ParticipantDeletedEvent(
                    command.quizId,
                    command.participantId,
                    sequenceNumber = it.sequenceNumber + 1
                )) }
                .map {
                    undoneEventsCache.remove(it.quizId)
                    eventBus.post(it)
                }
    }

    @WriteLock
    override fun deleteQuestion(command: DeleteQuestionCommand): Mono<Unit> {
        logger.info("deleting question '{}' from quiz '{}'", command.questionId, command.quizId)
        return eventRepository.determineEvents(command.quizId)
                .reduce(Quiz(name = "")) { quiz: Quiz, event: Event -> event.process(quiz)}
                .filter { !it.finished }
                .switchIfEmpty(Mono.error(QuizFinishedException()))
                .flatMap { eventRepository.storeEvent(QuestionDeletedEvent(
                    command.quizId,
                    command.questionId,
                    sequenceNumber = it.sequenceNumber + 1
                )) }
                .map {
                    undoneEventsCache.remove(it.quizId)
                    eventBus.post(it)
                }
    }

    @WriteLock
    override fun buzzer(command: BuzzerCommand): Mono<Unit> {
        logger.info("'{}' buzzered in quiz '{}'", command.participantId, command.quizId)
        return eventRepository.determineEvents(command.quizId)
                .reduce(Quiz(name = "")) { quiz: Quiz, event: Event -> event.process(quiz) }
                .filter { !it.finished }
                .switchIfEmpty(Mono.error(QuizFinishedException()))
                .filter { it.hasParticipantWithId(command.participantId) }
                .filter { it.currentQuestionIsBuzzerQuestion() }
                .filter { it.nobodyHasBuzzered() }
                .flatMap { eventRepository.storeEvent(BuzzeredEvent(
                    command.quizId,
                    command.participantId,
                    sequenceNumber = it.sequenceNumber + 1
                )) }
                .map {
                    undoneEventsCache.remove(it.quizId)
                    eventBus.post(it)
                }
    }

    @WriteLock
    override fun estimate(command: EstimationCommand): Mono<Unit> {
        logger.info("'{}' estimated value '{}' in quiz '{}'", command.participantId, command.estimatedValue, command.quizId)
        return eventRepository.determineEvents(command.quizId)
                .reduce(Quiz(name = "")) { quiz: Quiz, event: Event -> event.process(quiz) }
                .filter { !it.finished }
                .switchIfEmpty(Mono.error(QuizFinishedException()))
                .filter { it.hasParticipantWithId(command.participantId) }
                .filter { it.currentQuestionIsFreetextQuestion() }
                .filter { it.currentAnswerIsDifferent(command.participantId, command.estimatedValue) }
                .flatMap { eventRepository.storeEvent(EstimatedEvent(
                    command.quizId,
                    command.participantId,
                    command.estimatedValue,
                    sequenceNumber = it.sequenceNumber + 1
                )) }
                .map {
                    undoneEventsCache.remove(it.quizId)
                    eventBus.post(it)
                }
    }

    @WriteLock
    override fun selectChoice(command: SelectChoiceCommand): Mono<Unit> {
        logger.info("'{}' selected choice '{}' in quiz '{}'", command.participantId, command.choiceId, command.quizId)
        return eventRepository.determineEvents(command.quizId)
                .reduce(Quiz(name = "")) { quiz: Quiz, event: Event -> event.process(quiz) }
                .filter { !it.finished }
                .switchIfEmpty(Mono.error(QuizFinishedException()))
                .filter { it.currentQuestionIsMultipleChoiceQuestion() }
                .filter { it.currentChoiceIsDifferent(command.participantId, command.choiceId) }
                .flatMap { eventRepository.storeEvent(ChoiceSelectedEvent(
                    command.quizId,
                    command.participantId,
                    command.choiceId,
                    sequenceNumber = it.sequenceNumber + 1
                )) }
                .map {
                    undoneEventsCache.remove(it.quizId)
                    eventBus.post(it)
                }
    }

    @WriteLock
    override fun toggleAnswerRevealAllowed(command: ToggleAnswerRevealAllowedCommand): Mono<Unit> {
        logger.info("{} prevents the reveal of answers for quiz {}", command.participantId, command.quizId)
        return eventRepository.determineEvents(command.quizId)
                .reduce(Quiz(name = "")) { quiz: Quiz, event: Event -> event.process(quiz) }
                .filter { !it.finished }
                .switchIfEmpty(Mono.error(QuizFinishedException()))
                .filter { it.hasParticipantWithId(command.participantId) }
                .flatMap { eventRepository.storeEvent(ToggleAnswerRevealAllowedEvent(
                    command.quizId,
                    command.participantId,
                    sequenceNumber = it.sequenceNumber + 1
                )) }
                .map {
                    undoneEventsCache.remove(it.quizId)
                    eventBus.post(it)
                }
    }

    @WriteLock
    override fun startNewQuestion(command: AskQuestionCommand): Mono<Unit> {
        logger.info("starting question '{}' in quiz '{}'", command.questionId, command.quizId)
        return eventRepository.determineEvents(command.quizId)
                .reduce(Quiz(name = "")) { quiz: Quiz, event: Event -> event.process(quiz)}
                .filter { !it.finished }
                .switchIfEmpty(Mono.error(QuizFinishedException()))
                .flatMap { eventRepository.storeEvent(QuestionAskedEvent(
                    command.quizId,
                    command.questionId,
                    sequenceNumber = it.sequenceNumber + 1
                )) }
                .map { eventBus.post(it) }
                .flatMapMany { eventRepository.determineEvents(command.quizId) }
                .reduce(Quiz(name = "")) { quiz: Quiz, event: Event -> event.process(quiz) }
                .map {
                    val pendingQuestion = it.pendingQuestion
                    stopCounter(it.id)
                    if (pendingQuestion?.initialTimeToAnswer != null) {
                        subscriptions[it.id] = Flux.interval(Duration.ofSeconds(1))
                            .takeUntil { second -> second + 1 >= pendingQuestion.initialTimeToAnswer.toLong() }
                            .flatMap { timeDecreased(command.quizId, pendingQuestion.id) }
                            .subscribe()

                    }
                    undoneEventsCache.remove(it.id)
                }
    }

    @WriteLock
    override fun rate(command: AnswerCommand): Mono<Unit> {
        logger.info("'{}' answered '{}' in quiz", command.participantId, command.answer, command.quizId)
        return eventRepository.determineEvents(command.quizId)
                .reduce(Quiz(name = "")) { quiz: Quiz, event: Event -> event.process(quiz)}
                .filter { !it.finished }
                .switchIfEmpty(Mono.error(QuizFinishedException()))
                .flatMap { eventRepository.storeEvent(AnsweredEvent(
                    command.quizId,
                    command.participantId,
                    command.answer,
                    sequenceNumber = it.sequenceNumber + 1
                )) }
                .map {
                    undoneEventsCache.remove(it.quizId)
                    eventBus.post(it)
                }
    }

    @WriteLock
    override fun reopenQuestion(command: ReopenCurrentQuestionCommand): Mono<Unit> {
        logger.info("reopening active question in quiz '{}'", command.quizId)
        return eventRepository.determineEvents(command.quizId)
                .reduce(Quiz(name = "")) { quiz: Quiz, event: Event -> event.process(quiz)}
                .filter { !it.finished }
                .switchIfEmpty(Mono.error(QuizFinishedException()))
                .flatMap { eventRepository.storeEvent(CurrentQuestionReopenedEvent(
                    command.quizId,
                    sequenceNumber = it.sequenceNumber + 1
                )) }
                .map { eventBus.post(it) }
                .flatMapMany { eventRepository.determineEvents(command.quizId) }
                .reduce(Quiz(name = "")) { quiz: Quiz, event: Event -> event.process(quiz) }
                .map {
                    val pendingQuestion = it.pendingQuestion
                    stopCounter(it.id)
                    if (pendingQuestion?.initialTimeToAnswer != null) {
                        subscriptions[it.id] = Flux.interval(Duration.ofSeconds(1))
                            .takeUntil { second -> second + 1 >= pendingQuestion.initialTimeToAnswer.toLong() }
                            .flatMap { timeDecreased(command.quizId, pendingQuestion.id) }
                            .subscribe()
                    }
                    undoneEventsCache.remove(it.id)
                }
    }

    @WriteLock
    private fun timeDecreased(quizId: UUID, questionId: UUID): Mono<Unit> {
        return eventRepository.determineEvents(quizId)
            .reduce(Quiz(name = "")) { quiz: Quiz, event: Event -> event.process(quiz)}
            .map { eventBus.post(TimeToAnswerDecreasedEvent(quizId, questionId, sequenceNumber = it.sequenceNumber + 1)) }
    }

    @WriteLock
    override fun revealAnswers(command: RevealAnswersCommand): Mono<Unit> {
        logger.info("reveal answers of active question in quiz '{}'", command.quizId)
        return eventRepository.determineEvents(command.quizId)
                .reduce(Quiz(name = "")) { quiz: Quiz, event: Event -> event.process(quiz)}
                .filter { !it.finished }
                .switchIfEmpty(Mono.error(QuizFinishedException()))
                .flatMap { eventRepository.storeEvent(AnswersRevealedEvent(
                    command.quizId,
                    sequenceNumber = it.sequenceNumber + 1
                )) }
                .map {
                    stopCounter(it.quizId)
                    it
                }
                .map {
                    undoneEventsCache.remove(it.quizId)
                    eventBus.post(it)
                }
    }

    @WriteLock
    override fun finishQuiz(command: FinishQuizCommand): Mono<Unit> {
        logger.info("finishing quiz '{}'", command.quizId)
        return eventRepository.determineEvents(command.quizId)
            .reduce(Quiz(name = "")) { quiz: Quiz, event: Event -> event.process(quiz)}
            .filter { !it.finished }
            .switchIfEmpty(Mono.error(QuizFinishedException()))
            .flatMap { eventRepository.storeEvent(QuizFinishedEvent(
                command.quizId,
                sequenceNumber = it.sequenceNumber + 1
            )) }
            .map {
                undoneEventsCache.remove(it.quizId)
                eventBus.post(it)
            }
    }

    @WriteLock
    override fun deleteQuiz(command: DeleteQuizCommand): Mono<Unit> {
        logger.info("deleting quiz '{}'", command.quizId)
        return eventRepository.deleteEvents(command.quizId)
                .map {
                    subscriptions.remove(command.quizId)
                    undoneEventsCache.remove(command.quizId)
                    eventBus.post(QuizDeletedEvent(command.quizId, sequenceNumber = -1))
                }
    }

    @ReadLock
    override fun determineQuizzes(): Flux<Quiz> {
        logger.info("determine all quizzes")
        return eventRepository.determineQuizIds()
                .flatMap {
                    eventRepository.determineEvents(it)
                            .reduce(Quiz(name = "")) { quiz: Quiz, event: Event -> event.process(quiz) }
                }
    }

    @WriteLock
    override fun undo(command: UndoCommand): Mono<Unit> {
        return eventRepository.undoLastAction(command.quizId)
                .map { undoneEventsCache.push(it) }
                .map { eventBus.post(ReloadQuizCommand(command.quizId)) }
    }

    @WriteLock
    override fun redo(command: RedoCommand): Mono<Unit> {
        return if (undoneEventsCache.isEmpty(command.quizId)) {
            Mono.empty()
        } else {
            eventRepository.storeEvent(undoneEventsCache.pop(command.quizId))
                    .map { eventBus.post(it) }
        }
    }

    private fun stopCounter(quizId: UUID) {
        subscriptions[quizId]?.dispose()
        subscriptions.remove(quizId)
    }

}
