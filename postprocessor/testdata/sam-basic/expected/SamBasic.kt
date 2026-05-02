import java.util.Comparator

val comparator =
    Comparator<String> { left, right ->
        left.compareTo(right)
    }
