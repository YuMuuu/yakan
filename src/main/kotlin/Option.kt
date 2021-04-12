package main.kotlin

fun <B, A> Option<A>.flatmap(f: (A) -> Option<B>): Option<B> = when (this) {
    is Option.Some -> f(get)
    is Option.None -> Option.None
}

fun <A> Option<A>.isDefined(): Boolean {
    return when(this) {
        is Option.Some -> true
        is Option.None -> false
    }
}

fun <A> Option<A>.get(): A? {
    return when(this) {
        is Option.Some -> get
        is Option.None -> null
    }
}

sealed class Option<out A> {
    fun <B> map(f: (A) -> B): Option<B> = when (this) {
        is Some -> Some(f(get))
        is None -> None

    }

    fun <A>unit(f: () -> A) = Some(f)

    data class Some< out A>(val get: A): Option<A>()
    object None: Option<Nothing>()

}

