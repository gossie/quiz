package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

@Service
class DefaultQuizService(private val eventRepository: EventRepository,
                         private val eventBus: EventBus) : QuizService {

    private val logger = LoggerFactory.getLogger(QuizService::class.java)

    @WriteLock
    override fun createQuiz(command: CreateQuizCommand): Mono<Unit> {
        logger.debug("creating a new quiz")
        return eventRepository.storeEvent(QuizCreatedEvent(command.quizId, command.quiz))
                .map { eventBus.post(it) }
    }

    @WriteLock
    override fun createQuestion(command: CreateQuestionCommand): Mono<Unit> {
        logger.debug("creating a new question for quiz '{}'", command.quizId)
        return eventRepository.storeEvent(QuestionCreatedEvent(command.quizId, command.question))
                .map { eventBus.post(it) }
    }

    @WriteLock
    override fun editQuestion(command: EditQuestionCommand): Mono<Unit> {
        logger.debug("editing question '{}' of quiz '{}'", command.questionId, command.quizId)
        return eventRepository.storeEvent(QuestionEditedEvent(command.quizId, command.question))
                .map { eventBus.post(it) }
    }

    @WriteLock
    override fun createParticipant(command: CreateParticipantCommand): Mono<Unit> {
        logger.debug("creating a new participant in quiz '{}'", command.quizId)
        eventBus.post(ForceEmitCommand(command.quizId))
        return eventRepository.determineEvents(command.quizId)
                .reduce(Quiz(name = "")) { quiz: Quiz, event: Event -> event.process(quiz)}
                .filter { it.hasNoParticipantWithName(command.participant.name) }
                .flatMap { eventRepository.storeEvent(ParticipantCreatedEvent(command.quizId, command.participant)) }
                .map { eventBus.post(it) }
    }

    @WriteLock
    override fun deleteQuestion(command: DeleteQuestionCommand): Mono<Unit> {
        logger.debug("deleting question '{}' from quiz '{}'", command.questionId, command.quizId)
        return eventRepository.storeEvent(QuestionDeletedEvent(command.quizId, command.questionId))
                .map { eventBus.post(it) }
    }

    @WriteLock
    override fun buzzer(command: BuzzerCommand): Mono<Unit> {
        logger.debug("'{}' buzzered in quiz '{}'", command.participantId, command.quizId)
        return eventRepository.determineEvents(command.quizId)
                .reduce(Quiz(name = "")) { quiz: Quiz, event: Event -> event.process(quiz) }
                .filter { it.nobodyHasBuzzered() }
                .flatMap { eventRepository.storeEvent(BuzzeredEvent(command.quizId, command.participantId)) }
                .map { eventBus.post(it) }
    }

    @WriteLock
    override fun estimate(command: EstimationCommand): Mono<Unit> {
        logger.debug("'{}' estimated value '{}' in quiz '{}'", command.participantId, command.estimatedValue, command.quizId)
        return eventRepository.storeEvent(EstimatedEvent(command.quizId, command.participantId, command.estimatedValue))
                .map { eventBus.post(it) }
    }

    @WriteLock
    override fun startNewQuestion(command: AskQuestionCommand): Mono<Unit> {
        logger.debug("starting question '{}' in quiz '{}'", command.questionId, command.quizId)
        return eventRepository.storeEvent(QuestionAskedEvent(command.quizId, command.questionId))
                .map { eventBus.post(it) }
                .flatMapMany { eventRepository.determineEvents(command.quizId) }
                .reduce(Quiz(name = "")) { quiz: Quiz, event: Event -> event.process(quiz) }
                .map {
                    val pendingQuestion = it.pendingQuestion
                    if (pendingQuestion?.timeToAnswer != null) {
                        Flux.interval(Duration.ofSeconds(1))
                                .takeUntil { second -> second <= pendingQuestion.timeToAnswer }
                                .subscribe { eventBus.post(ForceEmitCommand(command.quizId)) }
                    }
                    Unit
                }
    }

    @WriteLock
    override fun answer(command: AnswerCommand): Mono<Unit> {
        logger.debug("'{}' answered '{}' in quiz", command.participantId, command.answer, command.quizId)
        return eventRepository.storeEvent(AnsweredEvent(command.quizId, command.participantId, command.answer))
                .map { eventBus.post(it) }
    }

    @WriteLock
    override fun reopenQuestion(command: ReopenCurrentQuestionCommand): Mono<Unit> {
        logger.debug("reopening active question in quiz '{}'", command.quizId)
        return eventRepository.storeEvent(CurrentQuestionReopenedEvent(command.quizId))
                .map { eventBus.post(it) }
    }

    @WriteLock
    override fun finishQuiz(command: FinishQuizCommand): Mono<Unit> {
        logger.debug("finishing quiz '{}'", command.quizId)
        return eventRepository.storeEvent(QuizFinishedEvent(command.quizId))
                .map { eventBus.post(it) }
    }

    @WriteLock
    override fun deleteQuiz(command: DeleteQuizCommand): Mono<Unit> {
        logger.debug("deleting quiz '{}'", command.quizId)
        return eventRepository.deleteEvents(command.quizId)
                .map { eventBus.post(QuizDeletedEvent(command.quizId)) }
    }

    @ReadLock
    override fun determineQuizzes(): Flux<Quiz> {
        logger.debug("determine all quizzes")
        return eventRepository.determineQuizIds()
                .flatMap {
                    eventRepository.determineEvents(it)
                            .reduce(Quiz(name = "")) { quiz: Quiz, event: Event -> event.process(quiz) }
                }
    }

}
