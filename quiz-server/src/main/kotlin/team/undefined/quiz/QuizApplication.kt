package team.undefined.quiz

import com.google.common.eventbus.EventBus
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import reactor.core.publisher.Flux
import team.undefined.quiz.core.DeleteQuizCommand
import team.undefined.quiz.core.QuizService
import java.time.Duration
import java.time.Period
import java.util.*

@SpringBootApplication
class QuizApplication {

    private val logger = LoggerFactory.getLogger(QuizApplication::class.java)
    private val ACTIVE_TIME = 2_419_200_000

    @Bean
    fun eventBus(): EventBus {
        return EventBus()
    }

    @Bean
    fun databaseCleaner(quizService: QuizService): CommandLineRunner {
        return (CommandLineRunner {
            Flux.interval(Duration.ofSeconds(30), Duration.ofHours(12))
                    .flatMap { quizService.determineQuizzes() }
                    .filter { it.getTimestamp() < (Date().time - ACTIVE_TIME) }
                    .flatMap { quizService.deleteQuiz(DeleteQuizCommand(it.id)) }
                    .subscribe { logger.info("deleted old quiz") }
        })
    }

}

fun main(args: Array<String>) {
    runApplication<QuizApplication>(*args)
}
