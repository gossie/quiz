package team.undefined.quiz.persistence

import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import team.undefined.quiz.core.Participant
import team.undefined.quiz.core.Quiz
import team.undefined.quiz.core.QuizRepository
import java.util.stream.Collectors

@Component
class DefaultQuizRepository(private val quizEntityRepository: QuizEntityRepository,
                            private val participantEntityRepository: ParticipantEntityRepository) : QuizRepository {

    override fun createQuiz(quiz: Quiz): Mono<Quiz> {
        return quizEntityRepository.save(quiz.map())
                .flatMap { it.map(Flux.empty()) }
    }

    override fun determineQuiz(id: Long): Mono<Quiz> {
        return quizEntityRepository.findById(id)
                .flatMap { it.map(participantEntityRepository.findByQuizId(id).map { it.map() }) }
    }

    override fun saveQuiz(quiz: Quiz): Mono<Quiz> {
        val participants = Flux.fromIterable(quiz.participants)
                .flatMap { participantEntityRepository.save(it.map(quiz)) }
                .map { it.map() }

        return quizEntityRepository.save(quiz.map())
                .flatMap { it.map(participants) }
    }

}

private fun QuizEntity.map(participants: Flux<Participant>): Mono<Quiz> {
    return participants
            .collect(Collectors.toList())
            .map { Quiz(this.id, this.name, it, emptyList(), getTurn(it)) }
}

private fun getTurn(participants: List<Participant>): String? {
    val turn = participants
            .filter { it.turn }
            .map { it.name }

    return if (turn.isEmpty()) null else turn[0]
}

private fun ParticipantEntity.map(): Participant {
    return Participant(this.id, this.name, this.turn == 1)
}

private fun Quiz.map(): QuizEntity {
    return QuizEntity(this.id, this.name)
}

private fun Participant.map(quiz: Quiz): ParticipantEntity {
    return ParticipantEntity(this.id, this.name, if (quiz.turn == this.name) 1 else 0, quiz.id!!)
}
