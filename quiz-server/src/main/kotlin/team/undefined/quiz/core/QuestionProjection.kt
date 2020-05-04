package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

@Component
final class QuestionProjection(eventBus: EventBus,
                               eventRepository: EventRepository) {

    private val questions = ConcurrentHashMap<UUID, Question>()

    init {
        eventBus.register(this)

        eventRepository.determineEvents()
                .filter { it is QuestionCreatedEvent || it is QuestionDeletedEvent }
                .subscribe {
                    if (it is QuestionCreatedEvent) {
                        handleQuestionCreation(it)
                    } else if (it is QuestionDeletedEvent) {
                        handleQuestionDeletion(it)
                    }
                }
    }

    @Subscribe
    fun handleQuestionCreation(event: QuestionCreatedEvent) {
        questions[event.question.id] = event.question
    }

    @Subscribe
    fun handleQuestionDeletion(event: QuestionDeletedEvent) {
        questions.remove(event.questionId)
    }

    fun determineQuestions(): Collection<String> {
        return questions.values.stream()
                .map { it.question }
                .distinct()
                .collect(Collectors.toList())
    }

}