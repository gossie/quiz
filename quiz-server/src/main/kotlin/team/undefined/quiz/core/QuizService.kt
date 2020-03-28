package team.undefined.quiz.core

import org.springframework.stereotype.Service
import reactor.core.publisher.EmitterProcessor
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class QuizService(private val quizRepository: QuizRepository) {

    private val emitterProcessor: EmitterProcessor<Quiz> = EmitterProcessor.create()

    fun createQuiz(quiz: Quiz): Mono<Quiz> {
        return quizRepository.createQuiz(quiz)
    }

    fun determineQuiz(quizId: Long): Mono<Quiz> {
        return quizRepository.determineQuiz(quizId)
    }

    fun createParticipant(quizId: Long, participantName: String): Mono<Quiz> {
        return quizRepository.determineQuiz(quizId)
                .map { it.addParticipant(Participant(name = participantName)) }
                .flatMap { quizRepository.saveQuiz(it) }
                .map {
                    emitterProcessor.onNext(it)
                    it
                }
    }

    fun buzzer(quizId: Long, participantName: String): Mono<Quiz> {
        return quizRepository.determineQuiz(quizId)
                .filter { it.nobodyHasBuzzered() }
                .map { it.select(participantName) }
                .flatMap { quizRepository.saveQuiz(it) }
                .map {
                    emitterProcessor.onNext(it)
                    it
                }
    }

    fun startNewQuestion(quizId: Long, question: String): Mono<Quiz> {
        return quizRepository.determineQuiz(quizId)
                .map { it.startQuestion(Question(question = question, pending = true)) }
                .flatMap { quizRepository.saveQuiz(it) }
                .map {
                    emitterProcessor.onNext(it)
                    it
                }
    }

    fun observeQuiz(): Flux<Quiz> {
        return emitterProcessor
    }


}
