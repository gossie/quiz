package team.undefined.quiz.web

import com.google.common.eventbus.EventBus
import io.restassured.RestAssured
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Mono
import team.undefined.quiz.core.*
import java.util.*


@SpringBootTest(classes = [BaseClass.Config::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = ["server.port=0"])
abstract class BaseClass {

    @LocalServerPort
    var port = 0

    @BeforeEach
    fun beforeEach() {
        RestAssured.baseURI = "http://localhost:" + this.port
    }

    @Configuration
    @EnableAutoConfiguration
    internal class Config {
        @Bean
        fun quizService(): QuizService {
            return (object : QuizService {
                override fun createQuiz(command: CreateQuizCommand): Mono<Unit> {
                    return Mono.just(Unit)
                }

                override fun createQuestion(command: CreateQuestionCommand): Mono<Unit> {
                    TODO("Not yet implemented")
                }

                override fun deleteQuestion(command: DeleteQuestionCommand): Mono<Unit> {
                    return Mono.just(Unit)
                }

                override fun createParticipant(command: CreateParticipantCommand): Mono<Unit> {
                    TODO("Not yet implemented")
                }

                override fun buzzer(command: BuzzerCommand): Mono<Unit> {
                    TODO("Not yet implemented")
                }

                override fun startNewQuestion(command: AskQuestionCommand): Mono<Unit> {
                    TODO("Not yet implemented")
                }

                override fun answer(command: AnswerCommand): Mono<Unit> {
                    TODO("Not yet implemented")
                }

                override fun reopenQuestion(command: ReopenCurrentQuestionCommand): Mono<Unit> {
                    TODO("Not yet implemented")
                }

                override fun finishQuiz(command: FinishQuizCommand): Mono<Unit> {
                    return Mono.just(command.quizId)
                            .filter { it == UUID.fromString("123e4567-e89b-12d3-a456-426655440000") }
                            .map { Unit }
                }

            })
        }

        @Bean
        fun quizProjection(): QuizProjection {
            return mock(QuizProjection::class.java)
        }

        @Bean
        fun questionProjection(): QuestionProjection {
            val questionProjection = mock(QuestionProjection::class.java)
            `when`(questionProjection.determineQuestions()).thenReturn(emptyMap())
            return questionProjection
        }

        @Bean
        fun eventBus(): EventBus {
            return mock(EventBus::class.java)
        }

        @Bean
        fun quizController(): QuizController {
            return QuizController(quizService(), quizProjection(), eventBus())
        }

        @Bean
        fun questionController(): QuestionController {
            return QuestionController(quizService())
        }

        @Bean
        fun questionPoolController(): QuestionPoolController {
            return QuestionPoolController(questionProjection())
        }
    }

}
