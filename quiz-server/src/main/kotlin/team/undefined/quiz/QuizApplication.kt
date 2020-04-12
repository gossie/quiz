package team.undefined.quiz

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import team.undefined.quiz.web.ReactiveWebSocketHandler
import com.google.common.eventbus.EventBus


@SpringBootApplication
class QuizApplication {

	@Bean
	fun eventBus(): EventBus {
		return EventBus()
	}

	@Bean
	fun webSocketHandlerMapping(webSocketHandler: ReactiveWebSocketHandler): HandlerMapping {
		return SimpleUrlHandlerMapping(mapOf(Pair("/event-emitter/{quizId}", webSocketHandler)), 1)
	}

	@Bean
	fun handlerAdapter() =  WebSocketHandlerAdapter()

}

fun main(args: Array<String>) {
	runApplication<QuizApplication>(*args)
}
