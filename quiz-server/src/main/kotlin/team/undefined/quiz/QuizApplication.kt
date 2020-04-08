package team.undefined.quiz

import io.r2dbc.spi.ConnectionFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ClassPathResource
import org.springframework.data.r2dbc.connectionfactory.init.CompositeDatabasePopulator
import org.springframework.data.r2dbc.connectionfactory.init.ConnectionFactoryInitializer
import org.springframework.data.r2dbc.connectionfactory.init.ResourceDatabasePopulator
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import team.undefined.quiz.web.ReactiveWebSocketHandler


@SpringBootApplication
class QuizApplication {

	@Bean
	fun initializer(connectionFactory: ConnectionFactory): ConnectionFactoryInitializer? {
		val initializer = ConnectionFactoryInitializer()
		initializer.setConnectionFactory(connectionFactory)
		val populator = CompositeDatabasePopulator()
		populator.addPopulators(ResourceDatabasePopulator(ClassPathResource("schema.sql")))
		initializer.setDatabasePopulator(populator)
		return initializer
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
