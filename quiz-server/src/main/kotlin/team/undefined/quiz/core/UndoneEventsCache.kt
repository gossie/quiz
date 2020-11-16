package team.undefined.quiz.core

import org.springframework.stereotype.Component
import java.util.*
import kotlin.collections.HashMap

@Component
class UndoneEventsCache {

    private val undoneEvents = HashMap<UUID, Stack<Event>>()

    fun push(event: Event) {
        val eventStack = undoneEvents.computeIfAbsent(event.quizId) { Stack() }
        eventStack.push(event)
    }

    fun pop(quizId: UUID): Event {
        return undoneEvents[quizId]!!.pop()
    }

    fun remove(quizId: UUID) {
        undoneEvents.remove(quizId)
    }

    fun isEmpty(quizId: UUID): Boolean {
        return undoneEvents[quizId]?.isEmpty() ?: true
    }

    fun isNotEmpty(quizId: UUID): Boolean {
        return !isEmpty(quizId)
    }

}