package team.undefined.quiz.core

import org.springframework.stereotype.Service
import reactor.core.publisher.EmitterProcessor
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

@Service
class QuizService(private val eventRepository: EventRepository) {

    private val observables = ConcurrentHashMap<Long, EmitterProcessor<Quiz>>()

    fun createQuiz(quiz: Quiz): Mono<Quiz> {
        return eventRepository.createQuiz(quiz)
    }

    fun determineQuiz(quizId: Long): Mono<Quiz> {
        return eventRepository.determineQuiz(quizId)
    }

    fun createQuestion(quizId: Long, question: String, imagePath: String = ""): Mono<Quiz> {
        return eventRepository.determineQuiz(quizId)
                .map { it.addQuestion(Question(question = question, imagePath = imagePath)) }
                .flatMap { eventRepository.saveQuiz(it) }
                .map { emitQuiz(it) }
    }

    fun createParticipant(quizId: Long, participantName: String): Mono<Quiz> {
        return eventRepository.determineQuiz(quizId)
                .map { it.addParticipantIfNecessary(Participant(name = participantName)) }
                .flatMap { eventRepository.saveQuiz(it) }
                .map { emitQuiz(it) }
    }

    fun buzzer(quizId: Long, participantId: Long): Mono<Quiz> {
        return eventRepository.determineQuiz(quizId)
                .filter { it.nobodyHasBuzzered() }
                .map { it.select(participantId) }
                .flatMap { eventRepository.saveQuiz(it) }
                .map { emitQuiz(it) }
    }

    fun startNewQuestion(quizId: Long, questionId: Long): Mono<Quiz> {
        return eventRepository.determineQuiz(quizId)
                .map { it.startQuestion(questionId) }
                .flatMap { eventRepository.saveQuiz(it) }
                .map { emitQuiz(it) }
    }

    fun correctAnswer(quizId: Long): Mono<Quiz> {
        return eventRepository.determineQuiz(quizId)
                .map { it.answeredCorrect() }
                .flatMap { eventRepository.saveQuiz(it) }
                .map { emitQuiz(it) }
    }

    fun incorrectAnswer(quizId: Long): Mono<Quiz> {
        return eventRepository.determineQuiz(quizId)
                .map { it.answeredInorrect() }
                .flatMap { eventRepository.saveQuiz(it) }
                .map { emitQuiz(it) }
    }

    fun reopenQuestion(quizId: Long): Mono<Quiz> {
        return eventRepository.determineQuiz(quizId)
                .map { it.reopenQuestion() }
                .flatMap { eventRepository.saveQuiz(it) }
                .map { emitQuiz(it) }
    }

    fun observeQuiz(quizId: Long): Flux<Quiz> {
        return observables.computeIfAbsent(quizId) {
            val emitter: EmitterProcessor<Quiz> = EmitterProcessor.create()
            emitter.doAfterTerminate { observables.remove(quizId) }
            emitter
        }
    }

    private fun emitQuiz(quiz: Quiz): Quiz {
        observables[quiz.id]?.onNext(quiz)
        return quiz
    }

    fun removeObserver(quizId: Long) {
        observables.remove(quizId)
    }

}
