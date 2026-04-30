# Edge-Case Dataset Report

This supplementary dataset is intentionally small and sharp. It is not meant to replace the real-world picocli benchmark; it isolates Java constructs that often expose weak spots in automatic Java-to-Kotlin conversion.

## Hypotheses

| Case | Hypothesis | Expected risk |
|---|---|---|
| `NestedAnonymousClasses.java` | The converter may preserve nested anonymous classes as verbose `object` expressions instead of simplifying SAM types to lambdas. | Kotlin output remains Java-shaped and hard to review. |
| `AnnotationDenseCommand.java` | Runtime annotation metadata and array-valued annotation parameters may be altered or dropped. | CLI/framework behavior can silently change. |
| `GenericWildcardsAndCheckedExceptions.java` | Bounded generics, wildcard variance, and checked exceptions may convert into overly broad nullable/platform types. | API contracts become less precise. |
| `OverloadsAndVarargs.java` | Java varargs and overloads may need `vararg`, spread operators, or `@JvmOverloads` to preserve call sites. | Generated Kotlin may compile but break Java callers. |

## How To Run

```bash
bash scripts/run-j2k.sh edge-cases converted/edge-cases
bash scripts/run-evaluator.sh edge-cases converted/edge-cases results/edge-cases
```

## Current Findings

The pipeline writes concrete results to `results/edge-cases/summary.md`, `metrics.csv`, and `metrics.json`. In the fallback converter mode, the expected failures are deliberately visible: semicolons and Java modifiers should remain as raw syntax markers, and anonymous classes should not become idiomatic Kotlin lambdas.

When run with a real JetBrains J2K wrapper via `J2K_CLI_JAR`, inspect these specific questions:

| Case | Pass condition | Failure signal |
|---|---|---|
| Nested anonymous classes | SAM conversions or valid `object : Comparator<String>` expressions are generated. | `new Comparator`, Java-style overrides, or malformed nested object syntax remains. |
| Annotation dense command | All annotations and array parameters are preserved. | `aliases`, `names`, or retention annotations disappear. |
| Generic wildcards | Kotlin variance and bounds are represented precisely. | Bounds collapse to `Any`, nullable types spread unnecessarily, or `throws` remains as raw Java syntax. |
| Overloads and varargs | `vararg` and spread calls are generated where needed. | Calls to vararg overloads fail or Java interop annotations are missing where needed. |

## Proposed Kotlin Fix Pattern

For the nested anonymous class case, a reviewer can simplify the generated Kotlin to a SAM/lambda form:

```kotlin
fun byLengthThenName(): Comparator<String> =
    Comparator { left, right ->
        val length = left.length.compareTo(right.length)
        if (length != 0) length else left.compareTo(right, ignoreCase = true)
    }
```

This is not a patch to JetBrains J2K itself; it is a concrete post-processing remediation pattern that could be encoded as a Kotlin rule: detect anonymous classes implementing single-method Java interfaces and rewrite them to lambdas when no instance state or multiple overrides are present.
