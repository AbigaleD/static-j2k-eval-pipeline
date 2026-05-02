import java.util.Comparator

val comparator =
    object : Comparator<String> {
        override fun compare(left: String, right: String): Int {
            return left.compareTo(right)
        }
    }
