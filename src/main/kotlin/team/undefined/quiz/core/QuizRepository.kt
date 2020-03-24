package team.undefined.quiz.core

import reactor.core.publisher.Mono

interface QuizRepository {

    fun createQuiz(quiz: Quiz): Mono<Quiz>

    fun determineQuiz(id: Long): Mono<Quiz>

}
