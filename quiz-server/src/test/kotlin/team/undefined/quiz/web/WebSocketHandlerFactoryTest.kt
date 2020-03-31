package team.undefined.quiz.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import team.undefined.quiz.core.QuizService


internal class WebSocketHandlerFactoryTest {

    @Test
    fun shouldCreateAWebSocketHandler() {
        val factory = WebSocketHandlerFactory(mock(QuizService::class.java), mock(ObjectMapper::class.java))
        assertThat(factory.createWebSocketHandler(1)).isNotNull
    }

    @Test
    fun shouldReturnTheSameHandlerForTheSameID() {
        val factory = WebSocketHandlerFactory(mock(QuizService::class.java), mock(ObjectMapper::class.java))
        val handler1 = factory.createWebSocketHandler(1)
        val handler2 = factory.createWebSocketHandler(1)

        assertThat(handler1).isNotNull
        assertThat(handler2).isNotNull
        assertThat(handler1).isSameAs(handler2)
    }

    @Test
    fun shouldReturnADifferentHandlerForADifferentID() {
        val factory = WebSocketHandlerFactory(mock(QuizService::class.java), mock(ObjectMapper::class.java))
        val handler1 = factory.createWebSocketHandler(1)
        val handler2 = factory.createWebSocketHandler(2)

        assertThat(handler1).isNotNull
        assertThat(handler2).isNotNull
        assertThat(handler1).isNotSameAs(handler2)
    }

}