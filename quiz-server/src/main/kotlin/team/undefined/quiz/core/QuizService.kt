package team.undefined.quiz.core

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface QuizService {

    fun createQuiz(command: CreateQuizCommand): Mono<Unit>

    fun createQuestion(command: CreateQuestionCommand): Mono<Unit>

    fun createParticipant(command: CreateParticipantCommand): Mono<Unit>

    fun deleteParticipant(command: DeleteParticipantCommand): Mono<Unit>

    fun buzzer(command: BuzzerCommand): Mono<Unit>

    fun estimate(command: EstimationCommand): Mono<Unit>

    fun toggleAnswerRevealAllowed(command: ToggleAnswerRevealAllowedCommand): Mono<Unit>

    fun startNewQuestion(command: AskQuestionCommand): Mono<Unit>

    fun deleteQuestion(command: DeleteQuestionCommand): Mono<Unit>

    fun editQuestion(command: EditQuestionCommand): Mono<Unit>

    fun answer(command: AnswerCommand): Mono<Unit>

    fun reopenQuestion(command: ReopenCurrentQuestionCommand): Mono<Unit>

    fun revealAnswers(command: RevealAnswersCommand): Mono<Unit>

    fun finishQuiz(command: FinishQuizCommand): Mono<Unit>

    fun deleteQuiz(command: DeleteQuizCommand): Mono<Unit>

    fun determineQuizzes(): Flux<Quiz>

}
