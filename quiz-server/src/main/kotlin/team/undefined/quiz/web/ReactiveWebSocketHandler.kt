package team.undefined.quiz.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import team.undefined.quiz.core.QuizProjection
import java.util.*

@Component
class ReactiveWebSocketHandler(private val quizProjection: QuizProjection,
                               private val objectMapper: ObjectMapper) : WebSocketHandler {

    override fun handle(webSocketSession: WebSocketSession): Mono<Void> {
        val quizId: UUID = determineQuizId(webSocketSession)
        return webSocketSession.send(quizProjection.observeQuiz(quizId)
                .map { QuizDTO(name = "muss ich noch fixen") }
                .map { objectMapper.writeValueAsString(it) }
                .map { webSocketSession.textMessage(it.toString()) })
                .and(webSocketSession.receive()
                        .map { it.payloadAsText }
                        .log())
                .doFinally {
                    quizProjection.removeObserver(quizId)
                }
    }

    private fun determineQuizId(webSocketSession: WebSocketSession): UUID {
        val components: List<String> = webSocketSession.handshakeInfo.uri.path.split("/")
        return UUID.fromString(components.last())
    }

}
