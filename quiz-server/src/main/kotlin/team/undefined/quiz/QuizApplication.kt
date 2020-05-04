package team.undefined.quiz

import com.google.common.eventbus.EventBus
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import team.undefined.quiz.core.QuizService

@SpringBootApplication
class QuizApplication {

	@Bean
	fun eventBus(): EventBus {
		return EventBus()
	}

	@Bean
	fun databaseCleaner(quizService: QuizService): CommandLineRunner {
		return (CommandLineRunner { TODO("Not yet implemented") })
	}

}

fun main(args: Array<String>) {
	runApplication<QuizApplication>(*args)
}
