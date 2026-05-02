# Edge-Case Dataset Report

This supplementary dataset is intentionally small and sharp. It is not meant to replace the real-world picocli benchmark; it isolates Java constructs that often expose weak spots in automatic Java-to-Kotlin conversion.

## Hypotheses

| Case | Hypothesis | Expected risk |
|---|---|---|
| `NestedAnonymousClasses.java` | The converter may preserve nested anonymous classes as verbose `object` expressions instead of simplifying SAM types to lambdas. | Kotlin output remains Java-shaped and hard to review. |
| `AnnotationDenseCommand.java` | Runtime annotation metadata and array-valued annotation parameters may be altered or dropped. | CLI/framework behavior can silently change. |
| `GenericWildcardsAndCheckedExceptions.java` | Bounded generics, wildcard variance, and checked exceptions may convert into overly broad nullable/platform types. | API contracts become less precise. |
| `OverloadsAndVarargs.java` | Java varargs and overloads may need `vararg`, spread operators, or `@JvmOverloads` to preserve call sites. | Generated Kotlin may compile but break Java callers. |
| `StaticInitializersAndConstants.java` | Static constants and static initializer blocks may not map cleanly to `const val`, companion objects, or `init` blocks. | Derived constants lose initialization order or Java interop shape. |
| `RawTypesAndLegacyCollections.java` | Raw collections and casts from `Object` may convert to unsafe Kotlin platform types or excessive casts. | Output compiles with weak types or becomes less idiomatic than the Java source. |
| `DefaultMethodsAndNestedInterfaces.java` | Default interface methods, nested interfaces, and nested implementations may convert into awkward interface/companion structures. | Default behavior or nested type references change. |
| `BuilderPatternWithFluentApi.java` | Fluent builders with private final fields may convert to verbose mutable code instead of idiomatic Kotlin constructors/defaults. | Builder chaining or validation semantics change. |

## How To Run

```bash
bash scripts/run-j2k.sh edge-cases converted/edge-cases
bash scripts/run-evaluator.sh edge-cases converted/edge-cases results/edge-cases
```

## Current Findings

Two runs are checked in: a CI smoke-test run using `static-fallback-translator` and a real `jetbrains-j2k-wrapper` run executed locally against IntelliJ IDEA 2025.2 with the bundled Kotlin plugin. Fallback output should not be read as JetBrains J2K quality; it exists only to keep the evaluator pipeline exercised in public CI where the internal wrapper is unavailable.

### Real JetBrains J2K wrapper (`results/edge-cases-real-j2k/`)

| Case | Status | Score | Main signal |
|---|---|---:|---|
| `BuilderPatternWithFluentApi.java` | needs-review | 65 | 10 Java-only syntax markers remain. |
| `DefaultMethodsAndNestedInterfaces.java` | needs-review | 84 | 4 Java-only syntax markers remain. |
| `StaticInitializersAndConstants.java` | good | 88 | Class/interface/enum count dropped by 1. |
| `RawTypesAndLegacyCollections.java` | good | 92 | 2 Java-only syntax markers remain. |
| `AnnotationDenseCommand.java` | good | 96 | 1 Java-only syntax marker remains. |
| `GenericWildcardsAndCheckedExceptions.java` | good | 96 | No heuristic issues detected. |
| `NestedAnonymousClasses.java` | good | 96 | No heuristic issues detected. |
| `OverloadsAndVarargs.java` | good | 96 | No heuristic issues detected. |

Average score: **91/100** — 6 good, 2 needs-review. The SAM post-processor rewrote anonymous-class expressions in `NestedAnonymousClasses.kt` to lambda form, raising the `anonymousConversionSignals` metric and validating the identified fix.

### Fallback CI smoke-test run (`results/edge-cases/`)

| Case | Status | Score | Main signal |
|---|---|---:|---|
| `RawTypesAndLegacyCollections.java` | poor | 64 | Method-like declaration count dropped by 4. |
| `BuilderPatternWithFluentApi.java` | needs-review | 65 | 21 Java-only syntax markers remain. |
| `DefaultMethodsAndNestedInterfaces.java` | needs-review | 72 | 5 Java-only syntax markers remain. |
| `NestedAnonymousClasses.java` | needs-review | 72 | 5 Java-only syntax markers remain; anonymous classes were not simplified. |
| `StaticInitializersAndConstants.java` | needs-review | 72 | 7 Java-only syntax markers remain. |
| `AnnotationDenseCommand.java` | needs-review | 80 | 5 Java-only syntax markers remain. |
| `OverloadsAndVarargs.java` | good | 88 | 3 Java-only syntax markers remain. |
| `GenericWildcardsAndCheckedExceptions.java` | good | 92 | 2 Java-only syntax markers remain. |

The SAM post-processor was run on the fallback output. It processed all 8 files but applied 0 conversions because the fallback translator does not produce Kotlin `object : Interface` expressions. With a real JetBrains J2K wrapper, the same step rewrites eligible single-method object expressions and the before/after improvement is visible in `results/edge-cases-postprocessed/`.

### Why `RawTypesAndLegacyCollections.java` is the hardest case

This file deliberately stacks three distinct converter hazards:

1. **Raw generic erasure.** `List list = new ArrayList()` gives the converter no element-type evidence. It must choose between `MutableList<Any?>` (safe but broad), a platform type `MutableList<*>!` (unsafe), or inferring from downstream usage (fragile).
2. **Unchecked `Object` casts.** Patterns like `(String) list.get(0)` force the converter to decide whether to emit `list[0] as String` (which adds a runtime check Kotlin already enforces) or `list[0] as? String` (which silently changes semantics if the cast fails).
3. **Overloaded collection APIs.** Methods such as `Collections.sort(list, comparator)` have multiple Java overloads; the correct Kotlin equivalent depends on whether the list type is known. Without it, the converter may pick the wrong overload or emit a call that requires an explicit spread or cast.

In the fallback run the structural evaluator catches this through dropped method-like declarations (the fallback conflates some overloads). In the real J2K run it scores 92 — still flagging 2 Java-only syntax markers — because the real converter preserves the call structure but leaves some unchecked cast idioms that a reviewer should verify.

When run with a real JetBrains J2K wrapper via `J2K_CLI_JAR`, inspect these specific questions:

| Case | Pass condition | Failure signal |
|---|---|---|
| Nested anonymous classes | SAM conversions or valid `object : Comparator<String>` expressions are generated. | `new Comparator`, Java-style overrides, or malformed nested object syntax remains. |
| Annotation dense command | All annotations and array parameters are preserved. | `aliases`, `names`, or retention annotations disappear. |
| Generic wildcards | Kotlin variance and bounds are represented precisely. | Bounds collapse to `Any`, nullable types spread unnecessarily, or `throws` remains as raw Java syntax. |
| Overloads and varargs | `vararg` and spread calls are generated where needed. | Calls to vararg overloads fail or Java interop annotations are missing where needed. |
| Static initializers | Constants and static initialization order are preserved. | `static`, non-constant `const val`, or changed initialization order remains. |
| Raw collections | Raw `List`/`Map` usage is converted with explicit, reviewable Kotlin types. | Unsafe casts are hidden or platform types become too broad. |
| Default methods and nested interfaces | Interface defaults and nested type references remain valid. | Default methods move incorrectly or nested references break. |
| Fluent builder | Builder chaining, validation, and immutable built object fields are preserved. | Setters stop returning the builder or validation moves after construction. |

## Implemented Kotlin Fix

The nested anonymous class failure is addressed by a working Kotlin post-processor:
[`postprocessor/src/main/kotlin/SamPostProcessor.kt`](../postprocessor/src/main/kotlin/SamPostProcessor.kt)

### What It Does

The post-processor scans `.kt` files for `object : InterfaceName` expressions that contain exactly one `override fun`. It rewrites them to SAM lambda form, handling nested anonymous classes by converting innermost expressions first.

### Running the Post-Processor

```bash
# Convert edge cases with real J2K wrapper first, then post-process
export J2K_CLI_JAR=/path/to/static-j2k-wrapper.jar
bash scripts/run-j2k.sh edge-cases converted/edge-cases

# Apply SAM fixes
bash scripts/run-postprocessor.sh \
  converted/edge-cases \
  converted/edge-cases-postprocessed \
  results/edge-cases/postprocessed

# Re-evaluate to measure improvement
bash scripts/run-evaluator.sh \
  edge-cases \
  converted/edge-cases-postprocessed \
  results/edge-cases-postprocessed
```

In the GitHub Actions workflow, these steps run automatically after the edge-case evaluation. The `results/edge-cases-postprocessed/` artifact shows quality scores after the fix is applied, providing a before/after comparison.

### Example Transformation

**Before** (typical J2K output for `NestedAnonymousClasses.java`):

```kotlin
object : Comparator<String> {
    override fun compare(left: String, right: String): Int {
        val nested = object : Comparator<String> {
            override fun compare(a: String, b: String): Int {
                return a.compareTo(b, ignoreCase = true)
            }
        }
        val length = Integer.compare(left.length(), right.length())
        return if (length != 0) length else nested.compare(left, right)
    }
}
```

**After** (post-processor output — inner expression converted first, then outer):

```kotlin
Comparator<String> { left, right ->
    val nested = Comparator<String> { a, b ->
        a.compareTo(b, ignoreCase = true)
    }
    val length = Integer.compare(left.length(), right.length())
    if (length != 0) length else nested.compare(left, right)
}
```

### Scope

This is a post-processing remediation, not a patch to JetBrains J2K itself. It encodes one clear rule: a single-abstract-method anonymous class with no instance state can always become a SAM lambda. The evaluator's `anonymousConversionSignals` metric should increase after the post-processor runs, directly measuring the fix's impact on the quality score.
