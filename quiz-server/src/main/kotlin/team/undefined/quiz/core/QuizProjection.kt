package team.undefined.quiz.core

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.util.*
import kotlin.collections.HashMap

@Component
class QuizProjection(eventBus: EventBus,
                     private val eventRepository: EventRepository) {

    private val quizCache: LoadingCache<UUID, Quiz> = CacheBuilder
            .newBuilder()
            .build(CacheLoader
                    .from<UUID, Quiz> {
                        eventRepository
                                .determineEvents(it!!)
                                .reduce(Quiz(name = "")) { quiz: Quiz, event: Event -> event.process(quiz)}
                                .block()
                    })

    init {
        eventBus.register(this)
    }

    @Subscribe
    fun handleQuizCreation(event: QuizCreatedEvent) {
        quizCache.put(event.quizId, event.quiz)
    }

    @Subscribe
    fun handleQuestionCreation(event: QuestionCreatedEvent) {
        quizCache.put(event.quizId, event.process(quizCache[event.quizId]))
    }

    fun determineQuiz(quizId: UUID): Quiz {
        return quizCache[quizId]
    }

    fun observeQuiz(quizId: UUID): Flux<Quiz> {
        TODO("Not yet implemented")
    }

    fun removeObserver(quizId: UUID) {
        TODO("Not yet implemented")
    }

}