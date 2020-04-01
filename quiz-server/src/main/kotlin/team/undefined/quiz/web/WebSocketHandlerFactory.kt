package team.undefined.quiz.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import team.undefined.quiz.core.QuizService

@Component
class WebSocketHandlerFactory(private val quizService: QuizService,
                              private val objectMapper: ObjectMapper) {

    fun createWebSocketHandler(quizId: Long): WebSocketHandler {
        return ReactiveWebSocketHandler(quizService, objectMapper, quizId)
    }

}