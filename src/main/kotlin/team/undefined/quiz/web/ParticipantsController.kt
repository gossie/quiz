package team.undefined.quiz.web

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import team.undefined.quiz.core.Quiz
import team.undefined.quiz.core.QuizService

@RestController
@CrossOrigin
@RequestMapping("/api/quiz/{quizId}/participants")
class ParticipantsController(private val quizService: QuizService) {

    @PutMapping("/{participantName}/buzzer", produces = ["application/json"])
    fun buzzer(@PathVariable quizId: Long, @PathVariable participantName: String): Mono<QuizDTO> {
        return quizService.buzzer(quizId, participantName)
                .map { it.map() };
    }

}