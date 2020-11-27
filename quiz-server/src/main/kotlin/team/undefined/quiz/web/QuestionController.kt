package team.undefined.quiz.web

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import team.undefined.quiz.core.*
import java.util.*

@RestController
@CrossOrigin(origins = ["*"], allowedHeaders = ["*"])
@RequestMapping("/api/quiz/{quizId}/questions")
class QuestionController(private val quizService: QuizService) {

    @PostMapping(consumes = ["application/json"])
    @ResponseStatus(HttpStatus.CREATED)
    fun createQuestion(@PathVariable quizId: UUID, @RequestBody question: QuestionDTO?): Mono<Unit> {
        return quizService.createQuestion(CreateQuestionCommand(quizId, question!!.map()))
                .onErrorResume { Mono.error(WebException(HttpStatus.CONFLICT, it.message)) }
    }

    @PutMapping("/{questionId}")
    @ResponseStatus(HttpStatus.OK)
    fun editQuestion(@PathVariable quizId: UUID, @PathVariable questionId: UUID, @RequestBody question: QuestionDTO): Mono<Unit> {
        return quizService.editQuestion(EditQuestionCommand(quizId, questionId, question.map(questionId)))
                .onErrorResume { Mono.error(WebException(HttpStatus.CONFLICT, it.message)) }
    }

    @PatchMapping("/{questionId}")
    @ResponseStatus(HttpStatus.OK)
    fun startQuestion(@PathVariable quizId: UUID, @PathVariable questionId: UUID): Mono<Unit> {
        return quizService.startNewQuestion(AskQuestionCommand(quizId, questionId))
                .onErrorResume { Mono.error(WebException(HttpStatus.CONFLICT, it.message)) }
    }

    @DeleteMapping("/{questionId}")
    fun deleteQuestion(@PathVariable quizId: UUID, @PathVariable questionId: UUID): Mono<Unit> {
        return quizService.deleteQuestion(DeleteQuestionCommand(quizId, questionId))
                .onErrorResume { Mono.error(WebException(HttpStatus.CONFLICT, it.message)) }
    }

}
