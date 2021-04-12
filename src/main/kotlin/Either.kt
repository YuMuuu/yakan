package main.kotlin

fun <E, B, A> Either<E, A>.flatmap(f: (A) -> Either<E, B>): Either<E, B> = when (this) {
    is Either.Right -> f(value)
    is Either.Left -> Either.Left(error)
}

sealed class Either<out E, out A> {
    fun <B> map(f: (A) -> B): Either<E, B> = when (this) {
        is Right -> Right(f(value))
        is Left -> Left(error)
    }


    data class Left<out E>(val error: E) : Either<E, Nothing>()
    data class Right<out A>(val value: A) : Either<Nothing, A>()
}

