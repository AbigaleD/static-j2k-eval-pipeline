# SAM Post-Processor Results

| Metric | Value |
|---|---:|
| Files processed | 8 |
| Files modified  | 0 |
| SAM conversions | 0 |

No SAM conversions were applied.

> In fallback-converter mode, J2K output retains Java syntax rather than producing
> `object :` expressions, so there is nothing for this post-processor to rewrite.
> Run with a real JetBrains J2K wrapper (`J2K_CLI_JAR`) to see conversions.

## What This Fix Addresses

Static J2K converts `new Interface() { @Override public T method(...) { ... } }`
to a verbose `object : Interface` expression instead of a Kotlin SAM lambda.
This post-processor detects the verbose form and rewrites it.

**Before** (J2K output):

```kotlin
object : Comparator<String> {
    override fun compare(left: String, right: String): Int {
        return left.compareTo(right)
    }
}
```

**After** (post-processor output):

```kotlin
Comparator<String> { left, right ->
    left.compareTo(right)
}
```

Nested anonymous classes are handled by converting innermost expressions first,
so deeply nested SAM types are fully simplified in a single pass.
