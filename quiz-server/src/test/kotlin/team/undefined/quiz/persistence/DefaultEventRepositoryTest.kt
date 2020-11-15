package team.undefined.quiz.persistence

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.context.annotation.Import
import reactor.test.StepVerifier
import team.undefined.quiz.core.*
import java.util.*

data class TestEvent(@JsonProperty("quizId") override val quizId: UUID, @JsonProperty("timestamp") override val timestamp: Long, @JsonProperty("payload") val payload: Map<String, String>) : Event {
    override fun process(quiz: Quiz): Quiz {
        TODO("Not yet implemented")
    }
}

@DataR2dbcTest
@Import(DefaultEventRepository::class, PersistenceConfiguration::class, DefaultQuizService::class, QuizProjection::class, ObjectMapper::class, QuizStatisticsProvider::class)
internal class DefaultEventRepositoryTest {

    @Autowired
    private lateinit var defaultEventRepository: DefaultEventRepository

    @Autowired
    private lateinit var eventEntityRepository: EventEntityRepository;

    @BeforeEach
    fun clearDB() {
        eventEntityRepository.deleteAll().subscribe()
    }

    @Test
    fun shouldStoreRetrieveAndDeleteEvents() {
        val firstQuizId = UUID.randomUUID()
        val secondQuizId = UUID.randomUUID()

        StepVerifier.create(defaultEventRepository.storeEvent(TestEvent(firstQuizId, Date().time, mapOf(Pair("key1", "value1")))))
                .consumeNextWith {
                    assertThat(it.quizId).isEqualTo(firstQuizId)
                    assertThat((it as TestEvent).payload).isEqualTo(mapOf(Pair("key1", "value1")))
                }
                .verifyComplete()

        Thread.sleep(1)

        StepVerifier.create(defaultEventRepository.storeEvent(TestEvent(firstQuizId, Date().time, mapOf(Pair("key2", "value2")))))
                .consumeNextWith {
                    assertThat(it.quizId).isEqualTo(firstQuizId)
                    assertThat((it as TestEvent).payload).isEqualTo(mapOf(Pair("key2", "value2")))
                }
                .verifyComplete()

        Thread.sleep(1)

        StepVerifier.create(defaultEventRepository.storeEvent(TestEvent(secondQuizId, Date().time, mapOf(Pair("key1", "value1")))))
                .consumeNextWith {
                    assertThat(it.quizId).isEqualTo(secondQuizId)
                    assertThat((it as TestEvent).payload).isEqualTo(mapOf(Pair("key1", "value1")))
                }
                .verifyComplete()

        Thread.sleep(1)

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

        StepVerifier.create(defaultEventRepository.determineEvents())
                .consumeNextWith {
                    assertThat(it.quizId).isEqualTo(firstQuizId)
                    assertThat((it as TestEvent).payload).isEqualTo(mapOf(Pair("key1", "value1")))
                }
                .consumeNextWith {
                    assertThat(it.quizId).isEqualTo(firstQuizId)
                    assertThat((it as TestEvent).payload).isEqualTo(mapOf(Pair("key2", "value2")))
                }
                .consumeNextWith {
                    assertThat(it.quizId).isEqualTo(secondQuizId)
                    assertThat((it as TestEvent).payload).isEqualTo(mapOf(Pair("key1", "value1")))
                }
                .verifyComplete()


        StepVerifier.create(defaultEventRepository.determineQuizIds())
                .consumeNextWith { it == firstQuizId }
                .consumeNextWith { it == secondQuizId }
                .verifyComplete()

        StepVerifier.create(defaultEventRepository.deleteEvents(firstQuizId))
                .consumeNextWith { it is Unit }
                .verifyComplete()

        StepVerifier.create(defaultEventRepository.determineEvents(firstQuizId))
                .verifyComplete()

        StepVerifier.create(defaultEventRepository.determineEvents())
                .consumeNextWith {
                    assertThat(it.quizId).isEqualTo(secondQuizId)
                    assertThat((it as TestEvent).payload).isEqualTo(mapOf(Pair("key1", "value1")))
                }
                .verifyComplete()
    }

}