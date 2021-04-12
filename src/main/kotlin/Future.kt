package main.kotlin

import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService

object Nonblocking {
    interface Future<out A> {
        fun ap(cb: (A) -> Unit)
    }

    object _Par {
        fun <A> run(es: ExecutorService, p: Par<A>): A {
            val ref = java.util.concurrent.atomic.AtomicReference<A>()
            val latch = CountDownLatch(1)
            p.invoke(es).ap {
                ref.set(it)
                latch.countDown()
            }
            latch.await()
            return ref.get()
        }

        fun <A> unit(a: A): Par<A> = {
            object : Future<A> {
                override fun ap(cb: (A) -> Unit) {
                    cb(a)
                }
            }
        }

        fun <A> delay(a: () -> A): Par<A> = {
            object : Future<A> {
                override fun ap(cb: (A) -> Unit) {
                    cb(a.invoke())
                }
            }
        }

        private fun eval(es: ExecutorService, r: () -> Unit): Unit {
            es.submit(Callable<Unit> { r.invoke() })
        }

        fun <A, B> map(p: Par<A>, b: (A) -> B): Par<B> = { es ->
            object : Future<B> {
                override fun ap(cb: (B) -> Unit) {
                    p(es).ap { a ->
                        eval(es) { cb(b(a)) }
                    }
                }

            }
        }

//        fun <A, B, C>map2(p1: Par<A>, p2: Par<B>, f: (A, B) -> B): Par<C> {
//            return { es ->
//                object : Future<C> {
//                    override fun ap(cb: (C) -> Unit) {
//                        var ar: Option<A> = None
//                        var br: Option<B> = None
//
//                        val combiner =  Actor<Either<A, B>>(
//                            object : Strategy {
//                                override fun <A> invoke(f: () -> A?): () -> A? {
//                                    TODO("Not yet implemented")
//                                }
//                            }
//                        , {
//                            when(it){
//                                is Either.Left -> {
//                                    if(br.isDefined()) {
////                                        return eval(es, cb(f { error(), br.get()!! }))
//                                        eval(es, { cb(f(error, br.get()!!)) })
//                                    }
//                                }
//                            }
//
//                            })
//                    }
//
//                }
//            }
//        }

    }
}

typealias Par<A> = (ExecutorService) -> Nonblocking.Future<A>