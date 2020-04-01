package team.undefined.quiz.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import team.undefined.quiz.core.QuizService
import java.util.concurrent.ConcurrentHashMap

@Component
class WebSocketHandlerFactory(private val quizService: QuizService,
                              private val objectMapper: ObjectMapper,
                              private val webSocketHandlerMapping: HandlerMapping) {

    private val handlerCache = ConcurrentHashMap<Long, ReactiveWebSocketHandler>()

    fun createWebSocketHandler(quizId: Long): WebSocketHandler {
        return handlerCache.computeIfAbsent(quizId) {
            val handler = ReactiveWebSocketHandler(quizService, objectMapper, quizId)
            handler.onClose()
                    .subscribe {
                        handlerCache.remove(quizId)
                        (webSocketHandlerMapping as SimpleUrlHandlerMapping).urlMap = mapOf(Pair("/event-emitter/$quizId", null))
                        webSocketHandlerMapping.initApplicationContext()
                    }
            handler
        }
    }

}