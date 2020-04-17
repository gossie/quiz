package team.undefined.quiz.web

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import team.undefined.quiz.core.*
import java.util.*

@RestController
@CrossOrigin(origins = ["*"], allowedHeaders = ["*"])
@RequestMapping("/api/quiz/{quizId}/questions")
class QuestionController(private val quizService: QuizService,
                         private val quizProjection: QuizProjection) {

    @PostMapping(consumes = ["application/json"], produces = ["application/json"])
    @ResponseStatus(HttpStatus.CREATED)
    fun createQuestion(@PathVariable quizId: UUID, @RequestBody question: QuestionDTO?): Mono<Unit> {
        return quizService.createQuestion(CreateQuestionCommand(quizId, Question(question = question!!.question)))
    }

    @PutMapping("/{questionId}", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    fun startQuestion(@PathVariable quizId: UUID, @PathVariable questionId: UUID): Mono<Unit> {
        return quizService.startNewQuestion(AskQuestionCommand(quizId, questionId))
    }

}
