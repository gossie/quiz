package team.undefined.quiz.core

import org.springframework.stereotype.Service
import reactor.core.publisher.EmitterProcessor
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class QuizService(private val quizRepository: QuizRepository) {

    private val emitterProcessor: EmitterProcessor<String> = EmitterProcessor.create()

    fun createQuiz(quiz: Quiz): Mono<Quiz> {
        return quizRepository.createQuiz(quiz)
    }

    fun determineQuiz(quizId: Long): Mono<Quiz> {
        return quizRepository.determineQuiz(quizId);
    }

    fun createParticipant(quizId: Long, participantName: String): Mono<Quiz> {
        return quizRepository.determineQuiz(quizId)
                .map { it.addParticipant(participantName) }
                .flatMap { quizRepository.saveQuiz(it) }
    }

    fun buzzer(quizId: Long, participantName: String): Mono<Quiz> {
        return quizRepository.determineQuiz(quizId)
                .filter { it.nobodyHasBuzzered() }
                .map { it.select(participantName) }
                .flatMap { quizRepository.saveQuiz(it) }
                .map {
                    emitterProcessor.onNext(it.turn!!)
                    it
                }
    }

    fun observeBuzzer(): Flux<String> {
        return emitterProcessor
    }



}
