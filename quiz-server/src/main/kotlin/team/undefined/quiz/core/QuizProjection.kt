package team.undefined.quiz.core

import reactor.core.publisher.Flux
import java.util.*

interface QuizProjection {

    fun observeQuiz(quizId: UUID): Flux<Quiz>

    fun determineQuiz(quizId: UUID): Quiz?

}