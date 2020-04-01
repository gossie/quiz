package team.undefined.quiz.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import team.undefined.quiz.core.QuizService

@Component
class ReactiveWebSocketHandler(private val quizService: QuizService,
                               private val objectMapper: ObjectMapper) : WebSocketHandler {

    override fun handle(webSocketSession: WebSocketSession): Mono<Void> {
        val quizId: Long = determineQuizId(webSocketSession)
        return webSocketSession.send(quizService.observeQuiz(quizId)
                .flatMap { it.map() }
                .map { objectMapper.writeValueAsString(it) }
                .map { webSocketSession.textMessage(it.toString()) })
                .and(webSocketSession.receive()
                        .map { it.payloadAsText }
                        .log())
                .doFinally {
                    quizService.removeObserver(quizId)
                }
    }

    private fun determineQuizId(webSocketSession: WebSocketSession): Long {
        val components: List<String> = webSocketSession.handshakeInfo.uri.path.split("/")
        return components.last().toLong()
    }

}
