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
                .onErrorResume { Mono.error(WebException(HttpStatus.CONFLICT, it.message)) }
    }

    @PutMapping("/{participantId}/buzzer", consumes = ["text/plain"])
    @ResponseStatus(HttpStatus.OK)
    fun buzzer(@PathVariable quizId: UUID, @PathVariable participantId: UUID, @RequestBody(required = false) estimation: String?): Mono<Unit> {
        return if (estimation == null) {
            quizService.buzzer(BuzzerCommand(quizId, participantId))
                    .onErrorResume { Mono.error(WebException(HttpStatus.CONFLICT, it.message)) }
        } else {
            quizService.estimate(EstimationCommand(quizId, participantId, estimation))
                    .onErrorResume { Mono.error(WebException(HttpStatus.CONFLICT, it.message)) }
        }
    }

    @PutMapping("/{participantId}/togglereveal")
    @ResponseStatus(HttpStatus.OK)
    fun toggleRevealPrevention(@PathVariable quizId: UUID, @PathVariable participantId: UUID): Mono<Unit> {
        return quizService.toggleAnswerRevealAllowed(ToggleAnswerRevealAllowedCommand(quizId, participantId))
                .onErrorResume { Mono.error(WebException(HttpStatus.CONFLICT, it.message)) }
    }

    @DeleteMapping("/{participantId}")
    @ResponseStatus(HttpStatus.OK)
    fun delete(@PathVariable quizId: UUID, @PathVariable participantId: UUID): Mono<Unit> {
        return quizService.deleteParticipant(DeleteParticipantCommand(quizId, participantId))
                .onErrorResume { Mono.error(WebException(HttpStatus.CONFLICT, it.message)) }
    }

}