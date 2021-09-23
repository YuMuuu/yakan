package main.kotlin.util

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

interface Strategy {
    fun <A> invoke(f: () -> A?): () -> A?

    companion object {
        fun fromExecutorService(es: ExecutorService): Strategy =
            object : Strategy {
                override fun <A> invoke(a: () -> A?): () -> A? {
                    val f = es.submit(
                        Callable<A>(a::invoke)
                    )
                    return f::get
                }
            }
    }
}

private class Node<A>(var a: A = null as A) : AtomicReference<Node<A>>()

open class Actor<A>(
    val strategy: Strategy,
    val handler: (A) -> Unit,
    val onError: (Throwable) -> Unit = { throw(it) }
) {
    private val tail = AtomicReference(Node<A>())
    private val suspended = AtomicInteger(1)
    private val head = AtomicReference(tail.get())

    //note: change "!"
    fun invoke(a: A): () -> A? {
        val n = Node(a)
        head.getAndSet(n).lazySet(n)
        return trySchedule()
    }

    private fun schedule(): () -> A? {
        return strategy.invoke { act().invoke() }
    }

    private fun act(): () -> A? {
        val t = tail.get()
        val n = batchHandle(t, 1024)

        return n?.let {
            it.a = null as A
            tail.lazySet(it)
            schedule()
        } ?: run {
            suspended.set(1)
            n?.get()?.let { trySchedule() } ?: { null }
        }
    }

    private fun trySchedule(): (() -> A?) {
        return if (suspended.compareAndSet(1, 0)) schedule()
        else {
            { null }
        }
    }

    private fun batchHandle(t: Node<A>?, i: Int): Node<A>? {
        val n: Node<A>? = t?.get()

        return n?.let {
            try {
                handler(it.a)
            } catch (e: Throwable) {
                onError(e)
            }
            if (i > 0) {
                batchHandle(n, i - 1)
            } else it
        } ?: run { t }

    }

    companion object {
        fun <A> invoke(
            es: ExecutorService,
            handler: (A) -> Unit,
            onError: (Throwable) -> Unit = { throw(it) }
        ): Actor<A> = Actor(Strategy.fromExecutorService(es), handler, onError)
    }
}
