package team.undefined.quiz.web

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import team.undefined.quiz.core.QuizService

@RestController
@CrossOrigin(origins = ["*"], allowedHeaders = ["*"])
@RequestMapping("/api/quiz/{quizId}/questions")
class QuestionController(private val quizService: QuizService) {

    @PostMapping(consumes = ["application/json"], produces = ["application/json"])
    @ResponseStatus(HttpStatus.CREATED)
    fun createQuestion(@PathVariable quizId: Long, @RequestBody question: QuestionDTO?): Mono<QuizDTO> {
        return quizService.createQuestion(quizId, question!!.question, question.imagePath)
                .flatMap { it.map() }
    }

    @PutMapping("/{questionId}", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    fun startQuestion(@PathVariable quizId: Long, @PathVariable questionId: Long): Mono<QuizDTO> {
        return quizService.startNewQuestion(quizId, questionId)
                .flatMap { it.map() }
    }

}
