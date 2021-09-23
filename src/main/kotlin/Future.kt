package main.kotlin

import main.kotlin.util.Actor
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService

object Nonblocking {
    interface Future<out A> {
        fun invoke(cb: (A) -> Unit)
    }

    object _Par {
        fun <A> run(es: ExecutorService, p: Par<A>): A {
            val ref = java.util.concurrent.atomic.AtomicReference<A>()
            val latch = CountDownLatch(1)
            p.invoke(es).invoke {
                ref.set(it)
                latch.countDown()
            }
            latch.await()
            return ref.get()
        }

        fun <A> unit(a: A): Par<A> = {
            object : Future<A> {
                override fun invoke(cb: (A) -> Unit) {
                    cb(a)
                }
            }
        }

        fun <A> fork(a: () -> Par<A>): Par<A> = { es ->
            object : Future<A> {
                override fun invoke(cb: (A) -> Unit) {
                    eval(es) { a.invoke().invoke(es).invoke(cb) }
                }
            }
        }

        fun <A> delay(a: () -> A): Par<A> = {
            object : Future<A> {
                override fun invoke(cb: (A) -> Unit) {
                    cb(a.invoke())
                }
            }
        }

        private fun eval(es: ExecutorService, r: () -> Unit): Unit {
            es.submit(Callable<Unit> { r.invoke() })
        }

        fun <A, B> map(p: Par<A>, b: (A) -> B): Par<B> = { es ->
            object : Future<B> {
                override fun invoke(cb: (B) -> Unit) {
                    p(es).invoke { a ->
                        eval(es) { cb(b(a)) }
                    }
                }

            }
        }

        fun <A, B, C> map2(p1: Par<A>, p2: Par<B>, f: (A, B) -> C): Par<C> = { es ->
            object : Future<C> {
                override fun invoke(cb: (C) -> Unit) {
                    var ar: Option<A> = Option.None
                    var br: Option<B> = Option.None

                    val combiner = Actor.invoke<Either<A, B>>(es, {
                        when (it) {
                            is Either.Left -> when (br) {
                                is Option.Some -> eval(es) { cb(f(it.error, (br as Option.Some<B>).get)) }
                                is Option.None -> ar = Option.Some(it.error)
                            }
                            is Either.Right -> when (ar) {
                                is Option.Some -> eval(es) { cb(f((ar as Option.Some<A>).get, it.value)) }
                                is Option.None -> br = Option.Some(it.value)
                            }
                        }

                    })

                    p1(es).invoke { combiner.invoke(Either.Left(it)) }
                    p2(es).invoke { combiner.invoke(Either.Right(it)) }
                }
            }
        }

        fun <A, B> flatMap(p: Par<A>, f: () -> Par<B>): Par<B> = { es ->
            object : Future<B> {
                override fun invoke(cb: (B) -> Unit) = p(es).invoke { f()(es).invoke(cb) }

            }
        }
    }
}

typealias Par<A> = (ExecutorService) -> Nonblocking.Future<A>