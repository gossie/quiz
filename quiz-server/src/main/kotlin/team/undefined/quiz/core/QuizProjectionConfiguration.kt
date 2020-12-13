package team.undefined.quiz.core

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
data class QuizProjectionConfiguration(
    @Value("\${quiz.cache.max-size}") val quizCacheMaxSize: Long,
    @Value("\${quiz.cache.duration-hours}") val quizCacheDuration: Long
)
