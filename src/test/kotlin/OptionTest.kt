
import main.kotlin.Either
import main.kotlin.Either.Right
import main.kotlin.Option
import main.kotlin.Option.Some
import main.kotlin.flatmap
import org.junit.Assert
import org.junit.Test

class OptionTest {
    @Test
    fun leftIdentityElement() {
        fun f(x: Int): Option<Int> {
            return Some(x * 2)
        }

        val n = Some(1).flatmap {
            f(it)
        }
        val m = f(1)


        Assert.assertEquals(n, m)
    }

    @Test
    fun rightIdentityElement() {
        val n = Some(1).flatmap {
            Some(it)
        }

        val m = Some(1)


        Assert.assertEquals(n, m)
    }

    @Test
    fun associativeLaw() {
        fun f(x: Int): Option<Int> {
            return Some(x + 1)
        }

        fun g(x: Int): Option<Int> {
            return Some(x * 3)
        }

        val n = Some(1).flatmap { it1 ->
            f(it1).flatmap { it2 ->
                g(it2)
            }
        }

        val m = Some(1).flatmap { it1 ->
            f(it1).flatmap { it2 ->
                g(it2)
            }
        }


        Assert.assertEquals(n, m)
    }
}


