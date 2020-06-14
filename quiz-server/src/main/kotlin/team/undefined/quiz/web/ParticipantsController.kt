package team.undefined.quiz.web

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import team.undefined.quiz.core.*
import java.util.*

@RestController
@CrossOrigin(origins = ["*"], allowedHeaders = ["*"])
@RequestMapping("/api/quiz/{quizId}/participants")
class ParticipantsController(private val quizService: QuizService) {

    @PostMapping(consumes = ["text/plain"])
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@PathVariable quizId: UUID, @RequestBody participantName: String): Mono<Unit> {
        return quizService.createParticipant(CreateParticipantCommand(quizId, Participant(name = participantName)))
    }

    @PutMapping("/{participantId}/buzzer", consumes = ["text/plain"])
    @ResponseStatus(HttpStatus.OK)
    fun buzzer(@PathVariable quizId: UUID, @PathVariable participantId: UUID, @RequestBody(required = false) estimation: String?): Mono<Unit> {
        if (estimation == null) {
            return quizService.buzzer(BuzzerCommand(quizId, participantId))
        } else {
            return quizService.estimate(EstimationCommand(quizId, participantId, estimation))
        }
    }

}