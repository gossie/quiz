package team.undefined.quiz.persistence

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.context.annotation.Import
import reactor.test.StepVerifier
import team.undefined.quiz.core.Event
import team.undefined.quiz.core.Quiz
import team.undefined.quiz.core.QuizProjection
import team.undefined.quiz.core.QuizService
import team.undefined.quiz.web.ReactiveWebSocketHandler
import java.util.*

data class TestEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("timestamp") override val timestamp: Long, @JsonProperty("payload") val payload: Map<String, String>) : Event {
    override fun process(quiz: Quiz): Quiz {
        TODO("Not yet implemented")
    }
}

@DataR2dbcTest
@Import(DefaultEventRepository::class, PersistenceConfiguration::class, QuizService::class, QuizProjection::class, ReactiveWebSocketHandler::class, ObjectMapper::class)
internal class DefaultEventRepositoryTest {

    @Autowired
    private lateinit var defaultEventRepository: DefaultEventRepository

    @Test
    fun shouldStoreAndRetreiveEvents() {
        val firstQuizId = UUID.randomUUID()
        val secondQuizId = UUID.randomUUID()

        StepVerifier.create(defaultEventRepository.storeEvent(TestEvent(firstQuizId, Date().time, mapOf(Pair("key1", "value1")))))
                .consumeNextWith {
                    assertThat(it.quizId).isEqualTo(firstQuizId)
                    assertThat((it as TestEvent).payload).isEqualTo(mapOf(Pair("key1", "value1")))
                }
                .verifyComplete()

        StepVerifier.create(defaultEventRepository.storeEvent(TestEvent(firstQuizId, Date().time, mapOf(Pair("key2", "value2")))))
                .consumeNextWith {
                    assertThat(it.quizId).isEqualTo(firstQuizId)
                    assertThat((it as TestEvent).payload).isEqualTo(mapOf(Pair("key2", "value2")))
                }
                .verifyComplete()

        StepVerifier.create(defaultEventRepository.storeEvent(TestEvent(secondQuizId, Date().time, mapOf(Pair("key1", "value1")))))
                .consumeNextWith {
                    assertThat(it.quizId).isEqualTo(secondQuizId)
                    assertThat((it as TestEvent).payload).isEqualTo(mapOf(Pair("key1", "value1")))
                }
                .verifyComplete()

        StepVerifier.create(defaultEventRepository.determineEvents(firstQuizId))
                .consumeNextWith {
                    assertThat(it.quizId).isEqualTo(firstQuizId)
                    assertThat((it as TestEvent).payload).isEqualTo(mapOf(Pair("key1", "value1")))
                }
                .consumeNextWith {
                    assertThat(it.quizId).isEqualTo(firstQuizId)
                    assertThat((it as TestEvent).payload).isEqualTo(mapOf(Pair("key2", "value2")))
                }
                .verifyComplete()
    }

}