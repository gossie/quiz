package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.util.*

internal class DefaultQuizServiceForbidChangesOnFinishedQuizTest {

    @Test
    fun shouldPreventCreationOfQuestionIfQuizIsAlreadyFinished() {
        val quizId = UUID.randomUUID()

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quizId)).thenReturn(
                Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Ein Quiz")),
                        QuizFinishedEvent(quizId, 1)
                )
        )

        val eventBus = mock(EventBus::class.java)

        val quizService = DefaultQuizService(eventRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.createQuestion(CreateQuestionCommand(quizId, Question(question = "Eine zu späte Frage"))))
                .verifyError()

        verifyNoInteractions(eventBus)
    }

    @Test
    fun shouldPreventEditingOfQuestionIfQuizIsAlreadyFinished() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quizId)).thenReturn(
                Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Ein Quiz")),
                        QuestionCreatedEvent(quizId, Question(questionId, "Eine Frage"), 1),
                        QuizFinishedEvent(quizId, 2)
                )
        )

        val eventBus = mock(EventBus::class.java)

        val quizService = DefaultQuizService(eventRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.editQuestion(EditQuestionCommand(quizId, questionId, Question(questionId, "Eine Frage"))))
                .verifyError(QuizFinishedException::class.java)

        verifyNoInteractions(eventBus)
    }

    @Test
    fun shouldPreventDeletionOfQuestionIfQuizIsAlreadyFinished() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quizId)).thenReturn(
                Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Ein Quiz")),
                        QuestionCreatedEvent(quizId, Question(questionId, "Eine Frage"), 1),
                        QuizFinishedEvent(quizId, 2)
                )
        )

        val eventBus = mock(EventBus::class.java)

        val quizService = DefaultQuizService(eventRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.deleteQuestion(DeleteQuestionCommand(quizId, questionId)))
                .verifyError(QuizFinishedException::class.java)

        verifyNoInteractions(eventBus)
    }

    @Test
    fun shouldPreventCreationOfParticipantIfQuizIsAlreadyFinished() {
        val quizId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quizId)).thenReturn(
                Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Ein Quiz")),
                        QuizFinishedEvent(quizId, 1)
                )
        )

        val eventBus = mock(EventBus::class.java)

        val quizService = DefaultQuizService(eventRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.createParticipant(CreateParticipantCommand(quizId, Participant(participantId, "Alex"))))
                .verifyError(QuizFinishedException::class.java)

        verify(eventBus).post(ForceEmitCommand(quizId))
        verifyNoMoreInteractions(eventBus)
    }

    @Test
    fun shouldPreventDeletionOfParticipantIfQuizIsAlreadyFinished() {
        val quizId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quizId)).thenReturn(
                Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Ein Quiz")),
                        ParticipantCreatedEvent(quizId, Participant(participantId, "Karo"), 1),
                        QuizFinishedEvent(quizId, 2)
                )
        )

        val eventBus = mock(EventBus::class.java)

        val quizService = DefaultQuizService(eventRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.deleteParticipant(DeleteParticipantCommand(quizId, participantId)))
                .verifyError(QuizFinishedException::class.java)

        verifyNoInteractions(eventBus)
    }

    @Test
    fun shouldPreventAskingOfQuestionIfQuizIsAlreadyFinished() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quizId)).thenReturn(
                Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Ein Quiz")),
                        QuestionCreatedEvent(quizId, Question(questionId, "Eine Frage"), 1),
                        QuizFinishedEvent(quizId, 2)
                )
        )

        val eventBus = mock(EventBus::class.java)

        val quizService = DefaultQuizService(eventRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.startNewQuestion(AskQuestionCommand(quizId, questionId)))
                .verifyError(QuizFinishedException::class.java)

        verifyNoInteractions(eventBus)
    }

    @Test
    fun shouldPreventBuzzerIfQuizIsAlreadyFinished() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quizId)).thenReturn(
                Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Ein Quiz")),
                        QuestionCreatedEvent(quizId, Question(questionId, "Eine Frage"), 1),
                        ParticipantCreatedEvent(quizId, Participant(participantId, "Landry"), 2),
                        QuestionAskedEvent(quizId, questionId, 3),
                        QuizFinishedEvent(quizId, 4)
                )
        )

        val eventBus = mock(EventBus::class.java)

        val quizService = DefaultQuizService(eventRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.buzzer(BuzzerCommand(quizId, participantId)))
                .verifyError(QuizFinishedException::class.java)

        verifyNoInteractions(eventBus)
    }

    @Test
    fun shouldPreventEstimateIfQuizIsAlreadyFinished() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quizId)).thenReturn(
                Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Ein Quiz")),
                        QuestionCreatedEvent(quizId, Question(questionId, "Eine Frage"), 1),
                        ParticipantCreatedEvent(quizId, Participant(participantId, "Beuke"), 2),
                        QuestionAskedEvent(quizId, questionId, 3),
                        QuizFinishedEvent(quizId, 4)
                )
        )

        val eventBus = mock(EventBus::class.java)

        val quizService = DefaultQuizService(eventRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.estimate(EstimationCommand(quizId, participantId, "32")))
                .verifyError(QuizFinishedException::class.java)

        verifyNoInteractions(eventBus)
    }

    @Test
    fun shouldPreventChoiceSelectionIfQuizIsAlreadyFinished() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()
        val choice1Id = UUID.randomUUID()
        val choice2Id = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quizId)).thenReturn(
                Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Ein Quiz")),
                        QuestionCreatedEvent(quizId, Question(questionId, "Eine Frage", choices = listOf(Choice(choice1Id, "a"), Choice(choice2Id, "b"))), 1),
                        ParticipantCreatedEvent(quizId, Participant(participantId, "Volker"), 2),
                        QuestionAskedEvent(quizId, questionId, 3),
                        QuizFinishedEvent(quizId, 4)
                )
        )

        val eventBus = mock(EventBus::class.java)

        val quizService = DefaultQuizService(eventRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.selectChoice(SelectChoiceCommand(quizId, participantId, choice1Id)))
                .verifyError(QuizFinishedException::class.java)

        verifyNoInteractions(eventBus)
    }

    @Test
    fun shouldPreventToggleIfQuizIsAlreadyFinished() {
        val quizId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quizId)).thenReturn(
                Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Ein Quiz")),
                        ParticipantCreatedEvent(quizId, Participant(participantId, "Karo"), 1),
                        QuizFinishedEvent(quizId, 2)
                )
        )

        val eventBus = mock(EventBus::class.java)

        val quizService = DefaultQuizService(eventRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.toggleAnswerRevealAllowed(ToggleAnswerRevealAllowedCommand(quizId, participantId)))
                .verifyError(QuizFinishedException::class.java)

        verifyNoInteractions(eventBus)
    }

    @Test
    fun shouldPreventRatingIfQuizIsAlreadyFinished() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quizId)).thenReturn(
                Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Ein Quiz")),
                        QuestionCreatedEvent(quizId, Question(questionId, "Eine Frage"), 1),
                        ParticipantCreatedEvent(quizId, Participant(participantId, "Landry"), 2),
                        QuestionAskedEvent(quizId, questionId, 3),
                        BuzzeredEvent(quizId, participantId, 4),
                        QuizFinishedEvent(quizId, 5)
                )
        )

        val eventBus = mock(EventBus::class.java)

        val quizService = DefaultQuizService(eventRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.rate(AnswerCommand(quizId, participantId, AnswerCommand.Answer.CORRECT)))
                .verifyError(QuizFinishedException::class.java)

        verifyNoInteractions(eventBus)
    }

    @Test
    fun shouldPreventReopenOfQuestionIfQuizIsAlreadyFinished() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quizId)).thenReturn(
                Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Ein Quiz")),
                        QuestionCreatedEvent(quizId, Question(questionId, "Eine Frage"), 1),
                        ParticipantCreatedEvent(quizId, Participant(participantId, "Landry"), 2),
                        QuestionAskedEvent(quizId, questionId, 3),
                        BuzzeredEvent(quizId, participantId, 4),
                        QuizFinishedEvent(quizId, 5)
                )
        )

        val eventBus = mock(EventBus::class.java)

        val quizService = DefaultQuizService(eventRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.reopenQuestion(ReopenCurrentQuestionCommand(quizId)))
                .verifyError(QuizFinishedException::class.java)

        verifyNoInteractions(eventBus)
    }

    @Test
    fun shouldPreventRevealOfAnswersIfQuizIsAlreadyFinished() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quizId)).thenReturn(
                Flux.just(
                        QuizCreatedEvent(quizId, Quiz(quizId, "Ein Quiz")),
                        QuestionCreatedEvent(quizId, Question(questionId, "Eine Frage"), 1),
                        ParticipantCreatedEvent(quizId, Participant(participantId, "Landry"), 2),
                        QuestionAskedEvent(quizId, questionId, 3),
                        EstimatedEvent(quizId, participantId, "Eine Antwort", 4),
                        QuizFinishedEvent(quizId, 5)
                )
        )

        val eventBus = mock(EventBus::class.java)

        val quizService = DefaultQuizService(eventRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.revealAnswers(RevealAnswersCommand(quizId)))
                .verifyError(QuizFinishedException::class.java)

        verifyNoInteractions(eventBus)
    }

    @Test
    fun shouldPreventFinishOfQuizIfQuizIsAlreadyFinished() {
        val quizId = UUID.randomUUID()
        val questionId = UUID.randomUUID()
        val participantId = UUID.randomUUID()

        val eventRepository = mock(EventRepository::class.java)
        `when`(eventRepository.determineEvents(quizId)).thenReturn(
            Flux.just(
                QuizCreatedEvent(quizId, Quiz(quizId, "Ein Quiz")),
                QuestionCreatedEvent(quizId, Question(questionId, "Eine Frage"), 1),
                ParticipantCreatedEvent(quizId, Participant(participantId, "Landry"), 2),
                QuestionAskedEvent(quizId, questionId, 3),
                EstimatedEvent(quizId, participantId, "Eine Antwort", 4),
                QuizFinishedEvent(quizId, 5)
            )
        )

        val eventBus = mock(EventBus::class.java)

        val quizService = DefaultQuizService(eventRepository, UndoneEventsCache(), eventBus)

        StepVerifier.create(quizService.finishQuiz(FinishQuizCommand(quizId)))
            .verifyError(QuizFinishedException::class.java)

        verifyNoInteractions(eventBus)
    }

}