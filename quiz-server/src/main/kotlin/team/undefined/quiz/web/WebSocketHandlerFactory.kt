package team.undefined.quiz.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import team.undefined.quiz.core.QuizService
import java.util.concurrent.TimeUnit

@Component
class WebSocketHandlerFactory(private val quizService: QuizService,
                              private val objectMapper: ObjectMapper) {

    private val handlerCache: LoadingCache<Long, ReactiveWebSocketHandler> = CacheBuilder
            .newBuilder()
            .expireAfterAccess(12, TimeUnit.HOURS)
            .build( object : CacheLoader<Long, ReactiveWebSocketHandler>() {
                override fun load(key: Long): ReactiveWebSocketHandler {
                    return ReactiveWebSocketHandler(quizService, objectMapper, key)
                }
            })

    fun createWebSocketHandler(quizId: Long): WebSocketHandler {
        return handlerCache[quizId]
    }

}