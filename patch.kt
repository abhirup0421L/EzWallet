import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

fun test(
    f1: Flow<Int>,
    f2: Flow<Int>,
    f3: Flow<Int>,
    f4: Flow<Int>,
    f5: Flow<Int>
) {
    combine(f1, f2, f3, f4, f5) { a, b, c, d, e ->
        a + b + c + d + e
    }
}
