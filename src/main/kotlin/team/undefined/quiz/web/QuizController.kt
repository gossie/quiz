package team.undefined.quiz.web

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import team.undefined.quiz.core.Quiz
import team.undefined.quiz.core.QuizService

@RestController
@RequestMapping("/api/quiz")
class QuizController(private val quizService: QuizService) {

    @PostMapping(produces = ["application/json"])
    @ResponseStatus(HttpStatus.CREATED)
    fun createQuiz(@RequestBody quiz: QuizDTO): Mono<QuizDTO> {
        return quizService.createQuiz(quiz.map())
                .map { it.map() }
    }

    @GetMapping("/{quizId}", produces = ["application/json"])
    fun determineQuiz(@PathVariable quizId: Long): Mono<QuizDTO> {
        return quizService.determineQuiz(quizId)
                .map { it.map() }
    }
}

private fun Quiz.map(): QuizDTO {
    return QuizDTO(this.name)
}

private fun QuizDTO.map(): Quiz {
    return Quiz(null, this.name)
}
