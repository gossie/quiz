package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference


internal class QuizProjectionConcurrencyTest {

    @Test
    @Disabled
    fun shouldHandleConcurrency() {
        for (i in 1..100) {
            val quiz = Quiz(name = "Awesome Quiz")

            val eventBus = EventBus()
            val quizProjection = QuizProjection(eventBus, Mockito.mock(QuizStatisticsProvider::class.java), Mockito.mock(EventRepository::class.java))

            val observedQuiz = AtomicReference<Quiz>()
            quizProjection.observeQuiz(quiz.id)
                    .subscribe { observedQuiz.set(it) }

            val question = Question(question = "Wofür steht die Abkürzung a.D.?", estimates = HashMap(), initialTimeToAnswer = 45)
            val participant = Participant(name = "Lena")
            eventBus.post(QuizCreatedEvent(quiz.id, quiz, 1))
            eventBus.post(QuestionCreatedEvent(quiz.id, question, 2))
            eventBus.post(ParticipantCreatedEvent(quiz.id, participant, 3))
            eventBus.post(QuestionAskedEvent(quiz.id, question.id, 4))

            val service = Executors.newFixedThreadPool(2)

            val latch = CountDownLatch(2)
            service.execute {
                eventBus.post(TimeToAnswerDecreasedEvent(quiz.id, question.id, 5))
                latch.countDown()
            }
            service.execute {
                eventBus.post(EstimatedEvent(quiz.id, participant.id, "My answer", 6))
                latch.countDown()
            }

            latch.await()

            await untilAsserted  {
                val q = observedQuiz.get()

                assertThat(q.id).isEqualTo(quiz.id)
                assertThat(q.participants).hasSize(1)
                assertThat(q.questions).hasSize(1)
                assertThat(q.questions[0].pending).isTrue()
                assertThat(q.questions[0].secondsLeft).isEqualTo(44)
                assertThat(q.questions[0].estimates!![participant.id]).isEqualTo("My answer")
                assertThat(q.finished).isFalse()
            }
        }
    }
}