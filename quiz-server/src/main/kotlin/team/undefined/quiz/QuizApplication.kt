package team.undefined.quiz

import com.google.common.eventbus.EventBus
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean


@SpringBootApplication
class QuizApplication {

	@Bean
	fun eventBus(): EventBus {
		return EventBus()
	}

}

fun main(args: Array<String>) {
	runApplication<QuizApplication>(*args)
}
