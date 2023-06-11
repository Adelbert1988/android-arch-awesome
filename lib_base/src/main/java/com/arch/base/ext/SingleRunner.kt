package com.guadou.lib_baselib.ext

import kotlinx.coroutines.CoroutineStart.LAZY
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield
import java.util.concurrent.atomic.AtomicReference

/**
 * 不常用
 * 单例的顺序执行-一次只能执行一个任务，一个任务执行完毕之后再执行下一个任务
 */
class SingleRunner {

    private val mutex = Mutex()

    /**
     * 加入到任务队列，前一个任务执行完毕再执行下一个任务
     */
    suspend fun <T> afterPrevious(block: suspend () -> T): T {
        mutex.withLock {
            return block()
        }
    }
}

class ControlledRunner<T> {

    private val activeTask = AtomicReference<Deferred<T>?>(null)

    suspend fun cancelPreviousThenRun(block: suspend () -> T): T {

        activeTask.get()?.cancelAndJoin()

        return coroutineScope {
            val newTask = async(start = LAZY) {
                block()
            }

            newTask.invokeOnCompletion {
                activeTask.compareAndSet(newTask, null)
            }

            val result: T

            while (true) {
                if (!activeTask.compareAndSet(null, newTask)) {
                    activeTask.get()?.cancelAndJoin()
                    yield()
                } else {
                    result = newTask.await()
                    break
                }
            }

            result
        }
    }

    /**
     * 不执行新任务，返回上一个任务的结果
     */
    suspend fun joinPreviousOrRun(block: suspend () -> T): T {

        activeTask.get()?.let {
            return it.await()
        }
        return coroutineScope {

            val newTask = async(start = LAZY) {
                block()
            }

            newTask.invokeOnCompletion {
                activeTask.compareAndSet(newTask, null)
            }

            val result: T

            while (true) {
                if (!activeTask.compareAndSet(null, newTask)) {

                    val currentTask = activeTask.get()
                    if (currentTask != null) {
                        newTask.cancel()
                        result = currentTask.await()
                        break
                    } else {
                        yield()
                    }
                } else {

                    result = newTask.await()
                    break
                }
            }
            result
        }
    }
}