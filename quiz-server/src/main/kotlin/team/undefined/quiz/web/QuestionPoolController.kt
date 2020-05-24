package team.undefined.quiz.web

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import team.undefined.quiz.core.QuestionProjection
import java.util.*

@RestController
@CrossOrigin(origins = ["*"], allowedHeaders = ["*"])
@RequestMapping("/api/questionPool")
class QuestionPoolController(private val questionProjection: QuestionProjection) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getQuestions(): Flux<QuestionDTO> {
        return Flux.fromIterable(determineQuestions())
    }

    private fun determineQuestions(): List<QuestionDTO> {
        return questionProjection
                .determineQuestions()
                .flatMap { entry ->
                    entry.value.map { it.map(entry.key) }
                }
    }

}