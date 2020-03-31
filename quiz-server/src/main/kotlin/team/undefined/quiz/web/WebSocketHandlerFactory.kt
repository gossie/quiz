package team.undefined.quiz.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import team.undefined.quiz.core.QuizService
import java.util.concurrent.ConcurrentHashMap

@Component
class WebSocketHandlerFactory(private val quizService: QuizService,
                              private val objectMapper: ObjectMapper) {

    private val handler = ConcurrentHashMap<Long, ReactiveWebSocketHandler>();

    fun createWebSocketHandler(quizId: Long): WebSocketHandler {
        return handler.computeIfAbsent(quizId) { ReactiveWebSocketHandler(quizService, objectMapper, quizId) }
    }

}