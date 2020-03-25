package team.undefined.quiz.persistence

import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import team.undefined.quiz.core.Quiz
import team.undefined.quiz.core.QuizRepository

@Component
class DefaultQuizRepository(private val quizEntityRepository: QuizEntityRepository) : QuizRepository {

    override fun createQuiz(quiz: Quiz): Mono<Quiz> {
        return quizEntityRepository.save(quiz.map())
                .map { it.map() }
    }

    override fun determineQuiz(id: Long): Mono<Quiz> {
        return quizEntityRepository.findById(id)
                .map { it.map() }
    }

    override fun saveQuiz(quiz: Quiz): Mono<Quiz> {
        return quizEntityRepository.save(quiz.map())
                .map { it.map() }
    }

}

private fun QuizEntity.map(): Quiz {
    val participants = if(this.participants.isEmpty()) emptyList() else this.participants.split(";")
    return Quiz(this.id, this.name, participants, this.turn)
}

private fun Quiz.map(): QuizEntity {
    return QuizEntity(this.id, this.name, this.participants.joinToString(";"), this.turn)
}
