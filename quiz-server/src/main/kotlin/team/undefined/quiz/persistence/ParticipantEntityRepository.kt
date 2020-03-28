package team.undefined.quiz.persistence

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface ParticipantEntityRepository : ReactiveCrudRepository<ParticipantEntity, Long> {

    @Query("SELECT * FROM PARTICIPANT_ENTITY WHERE QUIZ_ID = :quizId")
    fun findByQuizId(quizId: Long): Flux<ParticipantEntity>

}
