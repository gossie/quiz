package team.undefined.quiz.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.context.annotation.Import
import reactor.test.StepVerifier
import team.undefined.quiz.core.Event
import team.undefined.quiz.core.QuizService
import team.undefined.quiz.web.ReactiveWebSocketHandler
import java.util.*

@DataR2dbcTest
@Import(DefaultEventRepository::class, QuizService::class, ReactiveWebSocketHandler::class, ObjectMapper::class)
internal class DefaultEventRepositoryTest {

    @Autowired
    private lateinit var defaultEventRepository: DefaultEventRepository

    @Test
    fun shouldStoreAndRetreiveEvents() {
        data class TestEvent(override val quizId: UUID, override val timestamp: Long, val payload: Map<String, String>) : Event

        val firstQuizId = UUID.randomUUID()
        val secondQuizId = UUID.randomUUID()

        StepVerifier.create(defaultEventRepository.storeEvent(TestEvent(firstQuizId, Date().time, mapOf(Pair("key1", "value1")))))
                .expectNext(TestEvent(firstQuizId, Date().time, mapOf(Pair("key1", "value1"))))
                .verifyComplete()

        StepVerifier.create(defaultEventRepository.storeEvent(TestEvent(firstQuizId, Date().time, mapOf(Pair("key2", "value2")))))
                .expectNext(TestEvent(firstQuizId, Date().time, mapOf(Pair("key2", "value2"))))
                .verifyComplete()

        StepVerifier.create(defaultEventRepository.storeEvent(TestEvent(UUID.randomUUID(), Date().time, mapOf(Pair("key1", "value1")))))
                .expectNext(TestEvent(secondQuizId, Date().time, mapOf(Pair("key1", "value1"))))
                .verifyComplete()

        StepVerifier.create(defaultEventRepository.determineEvents(firstQuizId))
                .consumeNextWith {
                    assertThat(it.quizId).isEqualTo(17)
                    assertThat((it as TestEvent).payload).isEqualTo(mapOf(Pair("key1", "value1")))
                }
                .consumeNextWith {
                    assertThat(it.quizId).isEqualTo(17)
                    assertThat((it as TestEvent).payload).isEqualTo(mapOf(Pair("key2", "value2")))
                }
                .verifyComplete()
    }

}