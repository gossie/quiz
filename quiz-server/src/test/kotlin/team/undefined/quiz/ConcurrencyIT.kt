package team.undefined.quiz

import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.test.StepVerifier
import team.undefined.quiz.core.*
import team.undefined.quiz.core.QuizAssert.assertThat
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import java.util.stream.IntStream

@SpringBootTest
class ConcurrencyIT {

    @Autowired
    private lateinit var quizService: QuizService

    @Autowired
    private lateinit var quizProjection: QuizProjection

    @Test
    fun shouldHandleConcurrency() {
        IntStream
            .range(0, 250)
            .parallel()
            .forEach {
                val quiz = Quiz(name = "Awesome Quiz")
                val question = Question(quiz.id, question = "Wo befindet sich das Kahnbein?", initialTimeToAnswer = 45, estimates = HashMap())
                val participant1 = Participant(quiz.id, name = "Lena")
                val participant2 = Participant(quiz.id, name = "André")

                StepVerifier.create(quizService.createQuiz(CreateQuizCommand(quiz.id, quiz)))
                    .expectNext(Unit)
                    .verifyComplete()

                StepVerifier.create(quizService.createQuestion(CreateQuestionCommand(quiz.id, question)))
                    .expectNext(Unit)
                    .verifyComplete()

                StepVerifier.create(quizService.createParticipant(CreateParticipantCommand(quiz.id, participant1)))
                    .expectNext(Unit)
                    .verifyComplete()

                StepVerifier.create(quizService.createParticipant(CreateParticipantCommand(quiz.id, participant2)))
                    .expectNext(Unit)
                    .verifyComplete()

                StepVerifier.create(quizService.startNewQuestion(AskQuestionCommand(quiz.id, question.id)))
                    .expectNext(Unit)
                    .verifyComplete()

                val quizReference = AtomicReference<Quiz>()

                quizProjection.observeQuiz(quiz.id)
                    .subscribe { quizReference.set(it) }

                val latch = CountDownLatch(2)

                quizService.estimate(EstimationCommand(quiz.id, participant1.id, "In der Hand"))
                    .subscribe { latch.countDown() }

                quizService.estimate(EstimationCommand(quiz.id, participant2.id, "Im Fuß"))
                    .subscribe { latch.countDown() }

                latch.await(2, TimeUnit.SECONDS)

                await untilAsserted {
                    assertThat(quizReference.get())
                        .hasQuestion(0) {
                            it.hasEstimates(
                                mapOf(
                                    Pair(participant1.id, "In der Hand"),
                                    Pair(participant2.id, "Im Fuß")
                                )
                            )
                        }
                }

                StepVerifier.create(quizService.revealAnswers(RevealAnswersCommand(quiz.id)))
                    .expectNext(Unit)
                    .verifyComplete()

                await untilAsserted {
                    assertThat(quizReference.get())
                        .hasQuestion(0) { it.isRevealed }
                }

                StepVerifier.create(quizService.finishQuiz(FinishQuizCommand(quiz.id)))
                    .expectNext(Unit)
                    .verifyComplete()

                await untilAsserted {
                    assertThat(quizReference.get())
                        .isFinished
                }

                StepVerifier.create(quizService.deleteQuiz(DeleteQuizCommand(quiz.id)))
                    .expectNext(Unit)
                    .expectComplete()
            }
    }

}