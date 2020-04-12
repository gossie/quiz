package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import reactor.core.publisher.Flux
import java.util.*

class QuizProjection(private val eventBus: EventBus) {

    fun determineQuiz(quizId: UUID): Quiz {
        TODO("Not yet implemented")
    }

    fun observeQuiz(quizId: UUID): Flux<Quiz> {
        TODO("Not yet implemented")
    }

    fun removeObserver(quizId: UUID) {
        TODO("Not yet implemented")
    }

}