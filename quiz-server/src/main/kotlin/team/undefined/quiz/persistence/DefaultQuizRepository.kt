package team.undefined.quiz.persistence

import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import team.undefined.quiz.core.Participant
import team.undefined.quiz.core.Question
import team.undefined.quiz.core.Quiz
import team.undefined.quiz.core.QuizRepository
import java.util.stream.Collectors

@Component
class DefaultQuizRepository(private val quizEntityRepository: QuizEntityRepository,
                            private val participantEntityRepository: ParticipantEntityRepository,
                            private val questionEntityRepository: QuestionEntityRepository) : QuizRepository {

    override fun createQuiz(quiz: Quiz): Mono<Quiz> {
        return quizEntityRepository.save(quiz.map())
                .flatMap { it.map(Flux.empty(), Flux.empty()) }
    }

    override fun determineQuiz(id: Long): Mono<Quiz> {
        return quizEntityRepository.findById(id)
                .flatMap { it.map(participantEntityRepository.findByQuizId(id).map { it.map() }, questionEntityRepository.findByQuizId(id).map { it.map() }) }
    }

    override fun saveQuiz(quiz: Quiz): Mono<Quiz> {
        val participants = Flux.fromIterable(quiz.participants)
                .flatMap { participantEntityRepository.save(it.map(quiz)) }
                .map { it.map() }

        val questions = Flux.fromIterable(quiz.questions)
                .flatMap { questionEntityRepository.save(it.map(quiz)) }
                .map { it.map() }

        return quizEntityRepository.save(quiz.map())
                .flatMap { it.map(participants, questions) }
    }

}

private fun QuizEntity.map(participants: Flux<Participant>, questions: Flux<Question>): Mono<Quiz> {
    return participants
            .collect(Collectors.toList())
            .flatMap { fromQuestions(this, it, questions) }
}

private fun fromQuestions(quiz: QuizEntity, participants: List<Participant>, questions: Flux<Question>): Mono<Quiz> {
    return questions
            .collect(Collectors.toList())
            .map { Quiz(quiz.id, quiz.name, participants, it) }
}

private fun ParticipantEntity.map(): Participant {
    return Participant(this.id, this.name, this.turn == 1, this.points)
}

private fun QuestionEntity.map(): Question {
    return Question(this.id, this.question, this.pending == 1)
}

private fun Quiz.map(): QuizEntity {
    return QuizEntity(this.id, this.name)
}

private fun Participant.map(quiz: Quiz): ParticipantEntity {
    return ParticipantEntity(this.id, this.name, if (this.turn) 1 else 0, this.points, quiz.id!!)
}

private fun Question.map(quiz: Quiz): QuestionEntity {
    return QuestionEntity(this.id, this.question, if (this.pending) 1 else 0, quiz.id!!)
}
