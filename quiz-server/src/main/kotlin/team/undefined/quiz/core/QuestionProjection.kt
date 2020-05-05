package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import org.springframework.stereotype.Component
import java.util.concurrent.CopyOnWriteArrayList
import java.util.stream.Collectors

@Component
final class QuestionProjection(eventBus: EventBus,
                               eventRepository: EventRepository) {

    private val questions = CopyOnWriteArrayList<Question>()

    init {
        eventBus.register(this)

        eventRepository.determineEvents()
                .filter { it is QuestionCreatedEvent || it is QuestionDeletedEvent || it is QuestionAskedEvent }
                .subscribe {
                    if (it is QuestionCreatedEvent) {
                        handleQuestionCreation(it)
                    } else if (it is QuestionDeletedEvent) {
                        handleQuestionDeletion(it)
                    } else if (it is QuestionAskedEvent) {
                        handleQuestionAsked(it)
                    }
                }
    }

    @Subscribe
    fun handleQuestionCreation(event: QuestionCreatedEvent) {
        questions.add(event.question)
    }

    @Subscribe
    fun handleQuestionDeletion(event: QuestionDeletedEvent) {
        questions.removeIf { it.id == event.questionId }
    }

    @Subscribe
    private fun handleQuestionAsked(event: QuestionAskedEvent) {
        questions.find { it.id == event.questionId }
                ?.alreadyPlayed = true
    }

    fun determineQuestions(): Collection<Question> {
        return questions
                .filter { it.alreadyPlayed }
                .distinctBy { it.question }
    }

}