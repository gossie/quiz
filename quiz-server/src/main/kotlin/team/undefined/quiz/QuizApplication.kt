package team.undefined.quiz

import com.google.common.eventbus.EventBus
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import team.undefined.quiz.core.DeleteQuizCommand
import team.undefined.quiz.core.QuizService
import java.util.*

@SpringBootApplication
class QuizApplication {

	@Bean
	fun eventBus(): EventBus {
		return EventBus()
	}

	@Bean
	fun databaseCleaner(quizService: QuizService): CommandLineRunner {
		return (CommandLineRunner {
			quizService.determineQuizzes()
					.filter { it.getTimestamp() < (Date().time - 2_419_200_000) }
					.subscribe { quizService.deleteQuiz(DeleteQuizCommand(it.id)) }
		})
	}

}

fun main(args: Array<String>) {
	runApplication<QuizApplication>(*args)
}
