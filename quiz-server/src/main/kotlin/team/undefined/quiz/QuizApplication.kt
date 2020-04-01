package team.undefined.quiz

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import team.undefined.quiz.web.ReactiveWebSocketHandler


@SpringBootApplication
class QuizApplication {
	@Bean
	fun webSocketHandlerMapping(webSocketHandler: ReactiveWebSocketHandler): HandlerMapping {
		return SimpleUrlHandlerMapping(mapOf(Pair("/event-emitter/{quizId}", webSocketHandler)), 1)
	}

	@Bean
	fun handlerAdapter() =  WebSocketHandlerAdapter()
/*
	@Bean
	fun dataInitializer(quizService: QuizService): CommandLineRunner {
		return CommandLineRunner {
			quizService.createQuiz(Quiz(name = "Hegarty's Quiz", participants = listOf("Allli", "Sandra", "Erik")))
					.subscribe { println(it.id) }
		}
	}
*/
}

fun main(args: Array<String>) {
	runApplication<QuizApplication>(*args)
}
