package team.undefined.quiz

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter


@SpringBootApplication
class QuizApplication {
	@Bean
	fun webSocketHandlerMapping(webSocketHandler: WebSocketHandler): HandlerMapping {
		val map = HashMap<String, WebSocketHandler>()
		map["/event-emitter"] = webSocketHandler

		val handlerMapping = SimpleUrlHandlerMapping()
		handlerMapping.order = 1
		handlerMapping.urlMap = map
		return handlerMapping
	}

	@Bean
	fun handlerAdapter() =  WebSocketHandlerAdapter()
}

fun main(args: Array<String>) {
	runApplication<QuizApplication>(*args)
}

