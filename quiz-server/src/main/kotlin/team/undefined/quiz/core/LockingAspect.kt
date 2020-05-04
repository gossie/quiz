package team.undefined.quiz.core

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Semaphore

@Component
@Aspect
class LockingAspect {

    private val locks = ConcurrentHashMap<UUID, Semaphore>()

    @Around("execution(@WriteLock * *(*))")
    fun performLock(point: ProceedingJoinPoint): Mono<*> {
        val command = point.args[0] as Command
        val quizId = command.quizId
        val lock = locks.computeIfAbsent(quizId) { Semaphore(1) }

        lock.acquire()
        val mono = point.proceed() as Mono<*>
        return mono
                .doFinally { lock.release() }
    }

}