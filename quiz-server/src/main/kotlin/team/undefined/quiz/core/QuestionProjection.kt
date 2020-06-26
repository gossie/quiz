package team.undefined.quiz.core

import com.google.common.collect.MultimapBuilder
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.annotation.PostConstruct
import kotlin.collections.HashSet

@Component
class QuestionProjection(eventBus: EventBus,
                         private val eventRepository: EventRepository) {

    private val lock = ReentrantReadWriteLock()

    private val questions = MultimapBuilder.hashKeys().arrayListValues().build<UUID, Question>()

    init {
        eventBus.register(this)
    }

    @PostConstruct
    fun initializeEvents() {
        eventRepository.determineEvents()
                .filter { it is QuestionCreatedEvent || it is QuestionDeletedEvent || it is QuestionEditedEvent || it is QuestionAskedEvent || it is QuizDeletedEvent }
                .subscribe {
                    if (it is QuestionCreatedEvent) {
                        handleQuestionCreation(it)
                    } else if (it is QuestionDeletedEvent) {
                        handleQuestionDeletion(it)
                    } else if (it is QuestionEditedEvent) {
                        handleQuestionEdit(it)
                    } else if (it is QuestionAskedEvent) {
                        handleQuestionAsked(it)
                    } else if (it is QuizDeletedEvent) {
                        handleQuizDeletion(it)
                    }
                }
    }

    @Subscribe
    fun handleQuestionCreation(event: QuestionCreatedEvent) {
        try {
            lock.writeLock().lock()
            questions.put(event.quizId, event.question.copy())
        } finally {
            lock.writeLock().unlock()
        }
    }

    @Subscribe
    fun handleQuestionDeletion(event: QuestionDeletedEvent) {
        try {
            lock.writeLock().lock()
            questions[event.quizId].removeIf { it.id == event.questionId }
        } finally {
            lock.writeLock().unlock()
        }
    }

    @Subscribe
    fun handleQuestionEdit(event: QuestionEditedEvent) {
        try {
            lock.writeLock().lock()
            questions[event.quizId].replaceAll {
                if (it.id == event.question.id) {
                    event.question.copy()
                } else {
                    it
                }
            }
        } finally {
            lock.writeLock().unlock()
        }
    }

    @Subscribe
    fun handleQuestionAsked(event: QuestionAskedEvent) {
        try {
            lock.writeLock().lock()
            val question = questions.get(event.quizId).find { it.id == event.questionId }
            question?.alreadyPlayed = true
            question?.pending = true
        } finally {
            lock.writeLock().unlock()
        }
    }

    @Subscribe
    fun handleQuizDeletion(event: QuizDeletedEvent) {
        try {
            lock.writeLock().lock()
            questions.removeAll(event.quizId)
        } finally {
            lock.writeLock().unlock()
        }
    }

    fun determineQuestions(category: QuestionCategory): Map<UUID, List<Question>> {
        try {
            lock.readLock().lock()
            val proposedQuestions = HashMap<UUID, List<Question>>()

            val distinctOverMultipleQuizzes = HashSet<String>()

            questions.asMap().entries.forEach { entry ->
                val filteredQuestions = entry.value.asSequence()
                        .filter { it.category == category }
                        .filter { it.alreadyPlayed }
                        .filter { it.visibility == Question.QuestionVisibility.PUBLIC }
                        .filter { StringUtils.isEmpty(it.imageUrl) }
                        .filter { distinctOverMultipleQuizzes.add(it.question) }
                        .toList()

                if (filteredQuestions.isNotEmpty()) {
                    proposedQuestions[entry.key] = filteredQuestions
                }
            }

            return proposedQuestions
        } finally {
            lock.readLock().unlock()
        }
    }

}