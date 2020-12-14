package team.undefined.quiz.core

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
data class QuizStatisticsProjectionConfiguration(
    @Value("\${quiz-statistics.cache.max-size}") val quizStatisticsCacheMaxSize: Long,
    @Value("\${quiz-statistics.cache.duration-hours}") val quizStatisticsCacheDuration: Long
)
