package team.undefined.quiz.web

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import team.undefined.quiz.core.QuestionCategory
import team.undefined.quiz.core.QuestionProjection
import java.util.*

@RestController
@CrossOrigin(origins = ["*"], allowedHeaders = ["*"])
@RequestMapping("/api/questionPool")
class QuestionPoolController(private val questionProjection: QuestionProjection) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getQuestions(@RequestParam category: String): Flux<QuestionDTO> {
        return Flux.fromIterable(determineQuestions(category))
    }

    private fun determineQuestions(category: String): List<QuestionDTO> {
        return questionProjection
                .determineQuestions(QuestionCategory(category))
                .flatMap { entry ->
                    entry.value.map { it.map(entry.key) }
                }
    }

}