package team.undefined.quiz.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoProcessor
import reactor.netty.http.websocket.WebsocketInbound
import team.undefined.quiz.core.QuizService

class ReactiveWebSocketHandler(private val quizService: QuizService,
                               private val objectMapper: ObjectMapper,
                               private val quizId: Long) : WebSocketHandler {

    private val sessions = HashSet<WebSocketSession>()
    private val observable: MonoProcessor<Long> = MonoProcessor.create()

    override fun handle(webSocketSession: WebSocketSession): Mono<Void> {
        sessions.add(webSocketSession)
        return webSocketSession.send(quizService.observeQuiz(quizId)
                .flatMap { it.map() }
                .map { objectMapper.writeValueAsString(it) }
                .map { webSocketSession.textMessage(it.toString()) })
                .and(webSocketSession.receive()
                        .map { it.payloadAsText }
                        .log())
                .doFinally {
                    sessions.remove(webSocketSession)
                    if (sessions.isEmpty()) {
                        observable.onNext(quizId)
                    }
                }
    }

    fun onClose(): Mono<Long> {
        return observable
    }

}
