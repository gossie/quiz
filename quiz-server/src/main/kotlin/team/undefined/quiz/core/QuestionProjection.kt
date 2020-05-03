package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
final class QuestionProjection(eventBus: EventBus,
                               eventRepository: EventRepository) {

    private val questions = ConcurrentHashMap<UUID, Question>()

    init {
        eventBus.register(this)

        eventRepository.determineEvents()
                .filter { it is QuestionCreatedEvent }
                .subscribe { handleQuestionCreation(it as QuestionCreatedEvent) }
    }

    @Subscribe
    fun handleQuestionCreation(event: QuestionCreatedEvent) {
        questions[event.question.id] = event.question
    }

    fun determineQuestions(): Collection<Question> {
        return questions.values
    }

}