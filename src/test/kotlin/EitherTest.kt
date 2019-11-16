import main.kotlin.Either
import main.kotlin.Either.Right
import main.kotlin.flatmap
import org.junit.Assert
import org.junit.Test

class EitherTest {
    @Test
    fun leftIdentityElement() {
        fun f(x: Int): Either<Nothing, Int> {
            return Right(x * 2)
        }

        val n = Right(1).flatmap {
            f(it)
        }
        val m = f(1)


        Assert.assertEquals(n, m)
    }

    @Test
    fun rightIdentityElement() {
        val n = Right(1).flatmap {
            Right(it)
        }

        val m = Right(1)


        Assert.assertEquals(n, m)
    }

    @Test
    fun associativeLaw() {
        fun f(x: Int): Either<Nothing, Int> {
            return Right(x + 1)
        }

        fun g(x: Int): Either<Nothing, Int> {
            return Right(x * 3)
        }

        val n = Right(1).flatmap { it1 ->
            f(it1).flatmap { it2 ->
                g(it2)
            }
        }

        val m = Right(1).flatmap { it1 ->
            f(it1).flatmap { it2 ->
                g(it2)
            }
        }


        Assert.assertEquals(n, m)
    }
}


