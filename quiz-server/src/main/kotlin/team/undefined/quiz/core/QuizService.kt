package team.undefined.quiz.core

import org.springframework.stereotype.Service
import reactor.core.publisher.EmitterProcessor
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

@Service
class QuizService(private val quizRepository: QuizRepository) {

    private val observables = ConcurrentHashMap<Long, EmitterProcessor<Quiz>>()

    fun createQuiz(quiz: Quiz): Mono<Quiz> {
        return quizRepository.createQuiz(quiz)
    }

    fun determineQuiz(quizId: Long): Mono<Quiz> {
        return quizRepository.determineQuiz(quizId)
    }

    fun createQuestion(quizId: Long, question: String, imagePath: String = ""): Mono<Quiz> {
        return quizRepository.determineQuiz(quizId)
                .map { it.addQuestion(Question(question = question, imagePath = imagePath)) }
                .flatMap { quizRepository.saveQuiz(it) }
                .map { emitQuiz(it) }
    }

    fun createParticipant(quizId: Long, participantName: String): Mono<Quiz> {
        return quizRepository.determineQuiz(quizId)
                .map { it.addParticipantIfNecessary(Participant(name = participantName)) }
                .flatMap { quizRepository.saveQuiz(it) }
                .map { emitQuiz(it) }
    }

    fun buzzer(quizId: Long, participantId: Long): Mono<Quiz> {
        return quizRepository.determineQuiz(quizId)
                .filter { it.nobodyHasBuzzered() }
                .map { it.select(participantId) }
                .flatMap { quizRepository.saveQuiz(it) }
                .map { emitQuiz(it) }
    }

    fun startNewQuestion(quizId: Long, questionId: Long): Mono<Quiz> {
        return quizRepository.determineQuiz(quizId)
                .map { it.startQuestion(questionId) }
                .flatMap { quizRepository.saveQuiz(it) }
                .map { emitQuiz(it) }
    }

    fun correctAnswer(quizId: Long): Mono<Quiz> {
        return quizRepository.determineQuiz(quizId)
                .map { it.answeredCorrect() }
                .flatMap { quizRepository.saveQuiz(it) }
                .map { emitQuiz(it) }
    }

    fun incorrectAnswer(quizId: Long): Mono<Quiz> {
        return quizRepository.determineQuiz(quizId)
                .map { it.answeredInorrect() }
                .flatMap { quizRepository.saveQuiz(it) }
                .map { emitQuiz(it) }
    }

    fun reopenQuestion(quizId: Long): Mono<Quiz> {
        return quizRepository.determineQuiz(quizId)
                .map { it.reopenQuestion() }
                .flatMap { quizRepository.saveQuiz(it) }
                .map { emitQuiz(it) }
    }

    fun observeQuiz(quizId: Long): Flux<Quiz> {
        return observables.computeIfAbsent(quizId) {
            val emitter: EmitterProcessor<Quiz> = EmitterProcessor.create(false)
            emitter.doAfterTerminate { observables.remove(quizId) }
            emitter.doOnCancel { observables.remove(quizId) }
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
