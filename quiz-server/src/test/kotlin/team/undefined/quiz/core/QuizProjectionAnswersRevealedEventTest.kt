package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import reactor.core.publisher.Flux
import java.util.concurrent.atomic.AtomicReference

internal class QuizProjectionAnswersRevealedEventTest {

    @Test
    fun shouldHandleAnswersRevealedEventWhenQuizIsAlreadyInCache() {
        val quiz = Quiz(name = "Awesome Quiz")

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, Mockito.mock(QuizStatisticsProvider::class.java), Mockito.mock(EventRepository::class.java))

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
        eventBus.post(AnsweredEvent(quiz.id, participant.id, AnswerCommand.Answer.CORRECT, 6))

        await until {
            observedQuiz.get().id == quiz.id
                    && observedQuiz.get().participants.size == 1
                    && observedQuiz.get().participants[0].points == 2L
                    && observedQuiz.get().questions.size == 1
                    && observedQuiz.get().questions[0].pending
                    && !observedQuiz.get().questions[0].alreadyPlayed
                    && !observedQuiz.get().finished
        }
    }

    @Test
    fun shouldHandleAnswersRevealedEventWhenQuizIsNotInCacheAndLastEventWasAlreadyPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")

        val question = Question(question = "Wofür steht die Abkürzung a.D.?")
        val participant = Participant(name = "Lena")
        val answeredEvent = AnsweredEvent(quiz.id, participant.id, AnswerCommand.Answer.CORRECT, 6)

        val eventRepository = Mockito.mock(EventRepository::class.java)
        Mockito.`when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, 1),
                        QuestionCreatedEvent(quiz.id, question, 2),
                        ParticipantCreatedEvent(quiz.id, participant, 3),
                        QuestionAskedEvent(quiz.id, question.id, 4),
                        BuzzeredEvent(quiz.id, participant.id, 5),
                        answeredEvent
                ))

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, Mockito.mock(QuizStatisticsProvider::class.java), eventRepository)

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(answeredEvent)

        await until {
            observedQuiz.get().id == quiz.id
                    && observedQuiz.get().participants.size == 1
                    && observedQuiz.get().participants[0].points == 2L
                    && observedQuiz.get().questions.size == 1
                    && observedQuiz.get().questions[0].pending
                    && !observedQuiz.get().questions[0].alreadyPlayed
                    && !observedQuiz.get().finished
        }
    }

    @Test
    fun shouldHandleAnswersRevealedEventCreationWhenQuizIsNotInCacheAndLastEventWasNotYetPersisted() {
        val quiz = Quiz(name = "Awesome Quiz")
        val question = Question(question = "Wofür steht die Abkürzung a.D.?")
        val participant = Participant(name = "Lena")

        val eventRepository = Mockito.mock(EventRepository::class.java)
        Mockito.`when`(eventRepository.determineEvents(quiz.id))
                .thenReturn(Flux.just(
                        QuizCreatedEvent(quiz.id, quiz, 1),
                        QuestionCreatedEvent(quiz.id, question, 2),
                        ParticipantCreatedEvent(quiz.id, participant, 3),
                        QuestionAskedEvent(quiz.id, question.id, 4),
                        BuzzeredEvent(quiz.id, participant.id, 5)
                ))

        val eventBus = EventBus()
        val quizProjection = QuizProjection(eventBus, Mockito.mock(QuizStatisticsProvider::class.java), eventRepository)

        val observedQuiz = AtomicReference<Quiz>()
        quizProjection.observeQuiz(quiz.id)
                .subscribe { observedQuiz.set(it) }

        eventBus.post(AnsweredEvent(quiz.id, participant.id, AnswerCommand.Answer.CORRECT, 6))

        await until {
            observedQuiz.get().id == quiz.id
                    && observedQuiz.get().participants.size == 1
                    && observedQuiz.get().participants[0].points == 2L
                    && observedQuiz.get().questions.size == 1
                    && observedQuiz.get().questions[0].pending
                    && !observedQuiz.get().questions[0].alreadyPlayed
                    && !observedQuiz.get().finished
        }
    }

}