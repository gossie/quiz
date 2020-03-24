package team.undefined.quiz.core

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class QuizService(private val quizRepository: QuizRepository) {

    fun createQuiz(quiz: Quiz): Mono<Quiz> {
        return quizRepository.createQuiz(quiz)
    }

    fun determineQuiz(quizId: Long): Mono<Quiz> {
        return quizRepository.determineQuiz(quizId);
    }

}
