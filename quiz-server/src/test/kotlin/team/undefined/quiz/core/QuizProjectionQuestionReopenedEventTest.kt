package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import reactor.core.publisher.Flux
import java.util.concurrent.atomic.AtomicReference

internal class QuizProjectionQuestionReopenedEventTest {

    @Test
    fun shouldHandleQuestionReopenedEventWhenQuizIsAlreadyInCache() {
        val quiz = Quiz(name = "Awesome Quiz")

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), mock(EventRepository::class.java))

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        val question = Question(question = "Wofür steht die Abkürzung a.D.?")
        val participant = Participant(name = "Lena")

        eventBus.post(QuizCreatedEvent(quiz.id, quiz, 1))
        eventBus.post(QuestionCreatedEvent(quiz.id, question, 2))
        eventBus.post(ParticipantCreatedEvent(quiz.id, participant, 3))
        eventBus.post(QuestionAskedEvent(quiz.id, question.id, 4))
        eventBus.post(BuzzeredEvent(quiz.id, participant.id, 5))
        eventBus.post(AnsweredEvent(quiz.id, participant.id, AnswerCommand.Answer.INCORRECT, 6))
        eventBus.post(CurrentQuestionReopenedEvent(quiz.id, 7))

        await until {
            observedQuiz.get().id == quiz.id
                    && observedQuiz.get().participants.size == 1
                    && !observedQuiz.get().participants[0].turn
                    && observedQuiz.get().participants[0].points == 0L
                    && observedQuiz.get().questions.size == 1
                    && observedQuiz.get().questions[0].pending
                    && !observedQuiz.get().questions[0].alreadyPlayed
                    && !observedQuiz.get().finished
        }
    }

    @Test
    fun shouldHandleQuestionReopenedEventWhenQuizIsNotInCacheAndLastEventWasAlreadyPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")

        val question = Question(question = "Wofür steht die Abkürzung a.D.?")
        val participant = Participant(name = "Lena")
        val reopenedEvent = CurrentQuestionReopenedEvent(quiz.id, 7)

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, 1),
                        QuestionCreatedEvent(quiz.id, question, 2),
                        ParticipantCreatedEvent(quiz.id, Participant(name = "Lena"), 3),
                        QuestionAskedEvent(quiz.id, question.id, 4),
                        BuzzeredEvent(quiz.id, participant.id, 5),
                        AnsweredEvent(quiz.id, participant.id, AnswerCommand.Answer.INCORRECT, 6),
                        reopenedEvent
                ))

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), eventRepository)

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(reopenedEvent)

        await until {
            observedQuiz.get().id == quiz.id
                    && observedQuiz.get().participants.size == 1
                    && !observedQuiz.get().participants[0].turn
                    && observedQuiz.get().participants[0].points == 0L
                    && observedQuiz.get().questions.size == 1
                    && observedQuiz.get().questions[0].pending
                    && !observedQuiz.get().questions[0].alreadyPlayed
                    && !observedQuiz.get().finished
        }
    }

    @Test
    fun shouldHandleQuestionReopenedEventCreationWhenQuizIsNotInCacheAndLastEventWasNotYetPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")
        val question = Question(question = "Wofür steht die Abkürzung a.D.?")
        val participant = Participant(name = "Lena")

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, 1),
                        QuestionCreatedEvent(quiz.id, question, 2),
                        ParticipantCreatedEvent(quiz.id, participant, 3),
                        QuestionAskedEvent(quiz.id, question.id, 4),
                        BuzzeredEvent(quiz.id, participant.id, 5),
                        AnsweredEvent(quiz.id, participant.id, AnswerCommand.Answer.INCORRECT, 6)
                ))

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, mock(QuizStatisticsProvider::class.java), eventRepository)

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(CurrentQuestionReopenedEvent(quiz.id, 7))

        await until {
            observedQuiz.get().id == quiz.id
                    && observedQuiz.get().participants.size == 1
                    && !observedQuiz.get().participants[0].turn
                    && observedQuiz.get().participants[0].points == 0L
                    && observedQuiz.get().questions.size == 1
                    && observedQuiz.get().questions[0].pending
                    && !observedQuiz.get().questions[0].alreadyPlayed
                    && !observedQuiz.get().finished
        }
    }

}