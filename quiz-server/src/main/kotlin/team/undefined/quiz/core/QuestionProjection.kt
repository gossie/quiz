package team.undefined.quiz.core

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.Duration.ofSeconds
import java.util.concurrent.CopyOnWriteArrayList
import java.util.stream.Collectors
import javax.annotation.PostConstruct

@Component
class QuestionProjection(eventBus: EventBus,
                         eventRepository: EventRepository) {

    private val questions = CopyOnWriteArrayList<Question>()

    init {
        eventBus.register(this)

        Mono.delay(ofSeconds(10))
                .flatMapMany { eventRepository.determineEvents() }
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
        val question = questions.find { it.id == event.questionId }
        question?.alreadyPlayed = true
        question?.pending = true
    }

    fun determineQuestions(): List<Question> {
        return questions
                .filter { it.alreadyPlayed }
                .distinctBy { it.question }
    }

}