package team.undefined.quiz

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform
import org.springframework.boot.cloud.CloudPlatform
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import team.undefined.quiz.core.Event
import team.undefined.quiz.core.EventRepository
import java.io.BufferedReader
import java.io.InputStreamReader

@SpringBootApplication
class QuizApplication {

    private val logger = LoggerFactory.getLogger(QuizApplication::class.java)
    private val ACTIVE_TIME = 2_419_200_000

    @Bean
    fun eventBus(): EventBus {
        return EventBus()
    }


    @Bean
    @ConditionalOnCloudPlatform(CloudPlatform.HEROKU)
    fun events01(eventBus: EventBus, objectMapper: ObjectMapper, repo: EventRepository): CommandLineRunner {
        return (CommandLineRunner {
            Thread.sleep(5000)
            logger.info("import first batch of events")
            var counter = 0
            val values01 = readFileContent("01_events.json").getJSONArray("values")
            values01.forEach {
                val eventType = (it as JSONArray).get(2) as String
                val event = it.get(4) as String
                val domainEvent = objectMapper.readValue(event, Class.forName(eventType))
                val myEvent = Event::class.java.cast(domainEvent)
                repo.storeEvent(myEvent)
                    .subscribe { ev ->
                        logger.info("imported ${++counter} events")
                        eventBus.post(ev)
                    }
                Thread.sleep(100)
            }

            logger.info("import second batch of events")
            val values02 = readFileContent("02_events.json").getJSONArray("values")
            values02.forEach {
                val eventType = (it as JSONArray).get(2) as String
                val event = it.get(5) as String
                val domainEvent = objectMapper.readValue(event, Class.forName(eventType))
                val myEvent = Event::class.java.cast(domainEvent)
                repo.storeEvent(myEvent)
                    .subscribe { ev ->
                        eventBus.post(ev)
                        logger.info("imported ${++counter} events")
                    }
                Thread.sleep(100)
            }

            logger.info("import third batch of events")
            val values03 = readFileContent("03_events.json").getJSONArray("values")
            values03.forEach {
                val eventType = (it as JSONArray).get(2) as String
                val event = it.get(5) as String
                val domainEvent = objectMapper.readValue(event, Class.forName(eventType))
                val myEvent = Event::class.java.cast(domainEvent)
                repo.storeEvent(myEvent)
                    .subscribe { ev ->
                        eventBus.post(ev)
                        logger.info("imported ${++counter} events")
                    }
                Thread.sleep(100)
            }
        })
    }

    private fun readFileContent(filename: String): JSONObject {
        val input = javaClass.getResourceAsStream(filename)!!
        val buffer = BufferedReader(InputStreamReader(input));
        var content = ""
        var line = buffer.readLine()
        while (line != null) {
            content +=  line
            line = buffer.readLine();
        }
        return JSONObject(content)
    }

/*
    @Bean
    fun databaseCleaner(quizService: QuizService): CommandLineRunner {
        return (CommandLineRunner {
            Flux.interval(Duration.ofSeconds(30), Duration.ofHours(12))
                    .flatMap { quizService.determineQuizzes() }
                    .filter { it.timestamp < (Date().time - ACTIVE_TIME) }
                    .flatMap { quizService.deleteQuiz(DeleteQuizCommand(it.id)) }
                    .subscribe { logger.info("deleted old quiz") }
        })
    }
*/
}

fun main(args: Array<String>) {
    runApplication<QuizApplication>(*args)
}
