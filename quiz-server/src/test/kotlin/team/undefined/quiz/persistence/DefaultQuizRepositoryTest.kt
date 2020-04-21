package team.undefined.quiz.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.context.annotation.Import
import reactor.test.StepVerifier
import team.undefined.quiz.core.Participant
import team.undefined.quiz.core.Question
import team.undefined.quiz.core.Quiz
import team.undefined.quiz.core.QuizService
import java.util.concurrent.atomic.AtomicLong

@DataR2dbcTest
@Import(DefaultQuizRepository::class, ReactiveWebSocketHandler::class, QuizService::class, ObjectMapper::class)
internal class DefaultQuizRepositoryTest {

    @Autowired
    private lateinit var defaultQuizRepository: DefaultQuizRepository

    @Test
    fun shouldCreateAndDetermineChangeAndSaveQuiz() {
        val quizId = AtomicLong(-1)

        StepVerifier.create(defaultQuizRepository.createQuiz(Quiz(name = "Quiz")))
                .consumeNextWith {
                    quizId.set(it.id!!)
                    assertThat(it).isEqualTo(Quiz(it.id, "Quiz"))
                }
                .verifyComplete()

        StepVerifier.create(defaultQuizRepository.determineQuiz(quizId.get()))
                .expectNext(Quiz(quizId.get(), "Quiz"))
                .verifyComplete()

        val participant1Id = AtomicLong(-1)
        val participant2Id = AtomicLong(-1)
        val participant3Id = AtomicLong(-1)
        val questionId = AtomicLong(-1)

        StepVerifier.create(defaultQuizRepository.saveQuiz(Quiz(quizId.get(), "Quiz", listOf(Participant(name = "Sandra"), Participant(name = "Allli"), Participant(name = "Erik", turn = true)), listOf(Question(question = "Wofür steht eigentlich a.D.?")))))
                .consumeNextWith {
                    participant1Id.set(it.participants[0].id!!)
                    participant2Id.set(it.participants[1].id!!)
                    participant3Id.set(it.participants[2].id!!)
                    questionId.set(it.questions[0].id!!)

                    assertThat(it).isEqualTo(Quiz(quizId.get(), "Quiz", listOf(Participant(participant1Id.get(), "Sandra"), Participant(participant2Id.get(), "Allli"), Participant(participant3Id.get(), "Erik", true)), listOf(Question(questionId.get(), "Wofür steht eigentlich a.D.?"))))
                }
                .verifyComplete()

        StepVerifier.create(defaultQuizRepository.saveQuiz(Quiz(quizId.get(), "Quiz", listOf(Participant(participant1Id.get(), "Sandra"), Participant(participant2Id.get(), "Allli"), Participant(participant3Id.get(), "Erik", points = 1)), listOf(Question(questionId.get(), "Wofür steht eigentlich a.D.?")))))
                .expectNext(Quiz(quizId.get(), "Quiz", listOf(Participant(participant1Id.get(), "Sandra"), Participant(participant2Id.get(), "Allli"), Participant(participant3Id.get(), "Erik", points = 1)), listOf(Question(questionId.get(), "Wofür steht eigentlich a.D.?"))))
                .verifyComplete()

        StepVerifier.create(defaultQuizRepository.saveQuiz(Quiz(quizId.get(), "Quiz", listOf(Participant(participant1Id.get(), "Sandra"), Participant(participant2Id.get(), "Allli"), Participant(participant3Id.get(), "Erik", points = 1)), listOf(Question(questionId.get(), "Wofür steht eigentlich a.D.?", imagePath = "pathToImage")))))
                .expectNext(Quiz(quizId.get(), "Quiz", listOf(Participant(participant1Id.get(), "Sandra"), Participant(participant2Id.get(), "Allli"), Participant(participant3Id.get(), "Erik", points = 1)), listOf(Question(questionId.get(), "Wofür steht eigentlich a.D.?", imagePath = "pathToImage"))))
                .verifyComplete()

        StepVerifier.create(defaultQuizRepository.saveQuiz(Quiz(quizId.get(), "Quiz", listOf(Participant(participant1Id.get(), "Sandra"), Participant(participant2Id.get(), "Allli"), Participant(participant3Id.get(), "Erik", points = 1)), listOf(Question(questionId.get(), "Wofür steht eigentlich a.D.?", imagePath = "pathToImage", pending = true)))))
                .expectNext(Quiz(quizId.get(), "Quiz", listOf(Participant(participant1Id.get(), "Sandra"), Participant(participant2Id.get(), "Allli"), Participant(participant3Id.get(), "Erik", points = 1)), listOf(Question(questionId.get(), "Wofür steht eigentlich a.D.?", imagePath = "pathToImage", pending = true))))
                .verifyComplete()

        StepVerifier.create(defaultQuizRepository.saveQuiz(Quiz(quizId.get(), "Quiz", listOf(Participant(participant1Id.get(), "Sandra"), Participant(participant2Id.get(), "Allli"), Participant(participant3Id.get(), "Erik", points = 1)), listOf(Question(questionId.get(), "Wofür steht eigentlich a.D.?", imagePath = "pathToImage", alreadyPlayed = true)))))
                .expectNext(Quiz(quizId.get(), "Quiz", listOf(Participant(participant1Id.get(), "Sandra"), Participant(participant2Id.get(), "Allli"), Participant(participant3Id.get(), "Erik", points = 1)), listOf(Question(questionId.get(), "Wofür steht eigentlich a.D.?", imagePath = "pathToImage", alreadyPlayed = true))))
                .verifyComplete()
    }

}