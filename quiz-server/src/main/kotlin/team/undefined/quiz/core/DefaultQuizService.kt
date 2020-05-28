package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class DefaultQuizService(private val eventRepository: EventRepository,
                         private val eventBus: EventBus) : QuizService {

    @WriteLock
    override fun createQuiz(command: CreateQuizCommand): Mono<Unit> {
        return eventRepository.storeEvent(QuizCreatedEvent(command.quizId, command.quiz))
                .map { eventBus.post(it) }
    }

    @WriteLock
    override fun createQuestion(command: CreateQuestionCommand): Mono<Unit> {
        return eventRepository.storeEvent(QuestionCreatedEvent(command.quizId, command.question))
                .map { eventBus.post(it) }
    }

    @WriteLock
    override fun editQuestion(command: EditQuestionCommand): Mono<Unit> {
        return eventRepository.storeEvent(QuestionEditedEvent(command.quizId, command.question))
                .map { eventBus.post(it) }
    }

    @WriteLock
    override fun createParticipant(command: CreateParticipantCommand): Mono<Unit> {
        eventBus.post(ForceEmitCommand(command.quizId))
        return eventRepository.determineEvents(command.quizId)
                .reduce(Quiz(name = "")) { quiz: Quiz, event: Event -> event.process(quiz)}
                .filter { it.hasNoParticipantWithName(command.participant.name) }
                .flatMap { eventRepository.storeEvent(ParticipantCreatedEvent(command.quizId, command.participant)) }
                .map { eventBus.post(it) }
    }

    @WriteLock
    override fun deleteQuestion(command: DeleteQuestionCommand): Mono<Unit> {
        return eventRepository.storeEvent(QuestionDeletedEvent(command.quizId, command.questionId))
                .map { eventBus.post(it) }
    }

    @WriteLock
    override fun buzzer(command: BuzzerCommand): Mono<Unit> {
        return eventRepository.determineEvents(command.quizId)
                .reduce(Quiz(name = "")) { quiz: Quiz, event: Event -> event.process(quiz)}
                .filter { it.nobodyHasBuzzered() }
                .flatMap { eventRepository.storeEvent(BuzzeredEvent(command.quizId, command.participantId)) }
                .map { eventBus.post(it) }
    }

    @WriteLock
    override fun startNewQuestion(command: AskQuestionCommand): Mono<Unit> {
        return eventRepository.storeEvent(QuestionAskedEvent(command.quizId, command.questionId))
                .map { eventBus.post(it) }
    }

    @WriteLock
    override fun answer(command: AnswerCommand): Mono<Unit> {
        return eventRepository.storeEvent(AnsweredEvent(command.quizId, command.answer))
                .map { eventBus.post(it) }
    }

    @WriteLock
    override fun reopenQuestion(command: ReopenCurrentQuestionCommand): Mono<Unit> {
        return eventRepository.storeEvent(CurrentQuestionReopenedEvent(command.quizId))
                .map { eventBus.post(it) }
    }

    @WriteLock
    override fun finishQuiz(command: FinishQuizCommand): Mono<Unit> {
        return eventRepository.storeEvent(QuizFinishedEvent(command.quizId))
                .map { eventBus.post(it) }
    }

}
