package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class QuizService(private val eventRepository: EventRepository,
                  private val eventBus: EventBus) {

    fun createQuiz(command: CreateQuizCommand): Mono<Unit> {
        return eventRepository.storeEvent(QuizCreatedEvent(command.quizId, command.quiz))
                .map { eventBus.post(it) }
    }

    fun createQuestion(command: CreateQuestionCommand): Mono<Unit> {
        return eventRepository.storeEvent(QuestionCreatedEvent(command.quizId, command.question))
                .map { eventBus.post(it) }
    }

    fun createParticipant(command: CreateParticipantCommand): Mono<Unit> {
        return eventRepository.storeEvent(ParticipantCreatedEvent(command.quizId, command.participant))
                .map { eventBus.post(it) }
    }

    fun buzzer(command: BuzzerCommand): Mono<Unit> {
        return eventRepository.storeEvent(BuzzeredEvent(command.quizId, command.participantId))
                .map { eventBus.post(it) }
    }

    fun startNewQuestion(command: AskQuestionCommand): Mono<Unit> {
        return eventRepository.storeEvent(QuestionAskedEvent(command.quizId, command.questionId))
                .map { eventBus.post(it) }
    }

    fun answer(command: AnswerCommand): Mono<Unit> {
        return eventRepository.storeEvent(AnsweredEvent(command.quizId, command.answer))
                .map { eventBus.post(it) }
    }

    fun reopenQuestion(command: ReopenCurrentQuestionCommand): Mono<Unit> {
        return eventRepository.storeEvent(CurrentQuestionReopenedEvent(command.quizId))
                .map { eventBus.post(it) }
    }

}
