package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.concurrent.atomic.AtomicReference

internal class QuizProjectionAnswerRevealPreventedEventTest {

    @Test
    fun shouldHandleAnswerRevealPreventedEventWhenQuizIsAlreadyInCache() {
        val quiz = Quiz(name = "Awesome Quiz")

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, Mockito.mock(QuizStatisticsProvider::class.java), Mockito.mock(EventRepository::class.java), UndoneEventsCache())

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        val question = Question(question = "Wofür steht die Abkürzung a.D.?", estimates = HashMap())
        val participant1 = Participant(name = "Lena")
        val participant2 = Participant(name = "André")

        eventBus.post(QuizCreatedEvent(quiz.id, quiz, 1))
        eventBus.post(QuestionCreatedEvent(quiz.id, question, 2))
        eventBus.post(ParticipantCreatedEvent(quiz.id, participant1, 3))
        eventBus.post(ParticipantCreatedEvent(quiz.id, participant2, 4))
        eventBus.post(ToggleAnswerRevealAllowedEvent(quiz.id, participant2.id, 5))
        eventBus.post(QuestionAskedEvent(quiz.id, question.id, 6))
        eventBus.post(EstimatedEvent(quiz.id, participant1.id, "Antwort 1", 7))
        eventBus.post(EstimatedEvent(quiz.id, participant2.id, "Antwort 2", 8))
        eventBus.post(AnsweredEvent(quiz.id, participant1.id, AnswerCommand.Answer.CORRECT, 9))
        eventBus.post(AnswersRevealedEvent(quiz.id, 10))

        await untilAsserted  {
            val q = observedQuiz.get()

            assertThat(q.id).isEqualTo(quiz.id)
            assertThat(q.participants).hasSize(2)
            assertThat(q.participants[0].points).isEqualTo(2L)
            assertThat(q.participants[0].revealAllowed).isTrue()
            assertThat(q.participants[1].points).isEqualTo(0L)
            assertThat(q.participants[1].revealAllowed).isFalse()
            assertThat(q.questions).hasSize(1)
            assertThat(q.questions[0].pending).isTrue()
            assertThat(q.questions[0].revealed).isTrue()
            assertThat(q.questions[0].secondsLeft).isEqualTo(0)
            assertThat(q.questions[0].alreadyPlayed).isFalse()
            assertThat(q.isUndoPossible()).isTrue()
            assertThat(q.finished).isFalse()
        }
    }

}