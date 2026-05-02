# Static J2K Evaluation Summary

- Source project: `edge-cases/src/main/java`
- Converted project: `converted/edge-cases-real-j2k`
- Converter mode: `jetbrains-j2k-wrapper`
- Java files considered: 8
- Kotlin files produced: 8
- Matched Java/Kotlin pairs: 8
- File coverage: 100%
- Average quality score: 91/100
- Average suspiciousness: 9/100
- Status counts: good=6, needs-review=2
- Source hash: `6f26acd831cf6f0a`
- Converted hash: `62d757459c13c11f`

## Preservation Metrics

| Metric | Java source | Kotlin output |
|---|---:|---:|
| Classes | 9 | 10 |
| Interfaces | 4 | 2 |
| Enums | 0 | 0 |
| Methods/functions | 51 | 75 |
| Annotations | 13 | 14 |
| Anonymous classes/signals | 3 | 3 |

## Marker Metrics

- Java-only syntax markers remaining in Kotlin: 18
- TODO/ERROR/UnsupportedOperationException markers: 0

## Top 10 Most Suspicious Files

| Suspiciousness | Score | Status | File | Primary finding |
|---:|---:|---|---|---|
| 35 | 65 | needs-review | `cases/BuilderPatternWithFluentApi.java` | Kotlin output still contains 10 Java-only syntax markers. |
| 16 | 84 | needs-review | `cases/DefaultMethodsAndNestedInterfaces.java` | Kotlin output still contains 4 Java-only syntax markers. |
| 12 | 88 | good | `cases/StaticInitializersAndConstants.java` | Class/interface/enum/record declaration count dropped by 1. |
| 8 | 92 | good | `cases/RawTypesAndLegacyCollections.java` | Kotlin output still contains 2 Java-only syntax markers. |
| 4 | 96 | good | `cases/AnnotationDenseCommand.java` | Kotlin output still contains 1 Java-only syntax markers. |
| 0 | 100 | good | `cases/GenericWildcardsAndCheckedExceptions.java` | No heuristic issues detected. |
| 0 | 100 | good | `cases/NestedAnonymousClasses.java` | No heuristic issues detected. |
| 0 | 100 | good | `cases/OverloadsAndVarargs.java` | No heuristic issues detected. |

## Interpretation

The evaluator is intentionally structural: it does not claim semantic equivalence. It checks whether conversion preserved file coverage, declaration shape, annotations, method-like declarations, and whether generated Kotlin still contains Java-only syntax or unresolved markers. Files marked `poor` should be manually inspected first.

## Kotlin Compile Smoke Test

- Status: `failed`
- Kotlin files considered: 8
- Errors: 48
- Warnings: 0
- Diagnostics: `compile-smoke.md`
