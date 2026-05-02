# Static J2K Evaluation Summary

- Source project: `edge-cases`
- Converted project: `converted/edge-cases-postprocessed`
- Converter mode: `static-fallback-translator+sam-postprocessed`
- Java files considered: 8
- Kotlin files produced: 8
- Matched Java/Kotlin pairs: 8
- File coverage: 100%
- Average quality score: 76/100
- Average suspiciousness: 24/100
- Status counts: good=2, needs-review=5, poor=1
- Source hash: `9f54fde1d39b21c2`
- Converted hash: `356e0f57cf01348a`

## Preservation Metrics

| Metric | Java source | Kotlin output |
|---|---:|---:|
| Classes | 9 | 9 |
| Interfaces | 4 | 4 |
| Enums | 0 | 0 |
| Methods/functions | 51 | 48 |
| Annotations | 13 | 13 |
| Anonymous classes/signals | 3 | 0 |

## Marker Metrics

- Java-only syntax markers remaining in Kotlin: 54
- TODO/ERROR/UnsupportedOperationException markers: 0

## Top 10 Most Suspicious Files

| Suspiciousness | Score | Status | File | Primary finding |
|---:|---:|---|---|---|
| 36 | 64 | poor | `src/main/java/cases/RawTypesAndLegacyCollections.java` | Method-like declaration count dropped by 4. |
| 35 | 65 | needs-review | `src/main/java/cases/BuilderPatternWithFluentApi.java` | Kotlin output still contains 21 Java-only syntax markers. |
| 28 | 72 | needs-review | `src/main/java/cases/DefaultMethodsAndNestedInterfaces.java` | Kotlin output still contains 5 Java-only syntax markers. |
| 28 | 72 | needs-review | `src/main/java/cases/NestedAnonymousClasses.java` | Kotlin output still contains 5 Java-only syntax markers. |
| 28 | 72 | needs-review | `src/main/java/cases/StaticInitializersAndConstants.java` | Kotlin output still contains 7 Java-only syntax markers. |
| 20 | 80 | needs-review | `src/main/java/cases/AnnotationDenseCommand.java` | Kotlin output still contains 5 Java-only syntax markers. |
| 12 | 88 | good | `src/main/java/cases/OverloadsAndVarargs.java` | Kotlin output still contains 3 Java-only syntax markers. |
| 8 | 92 | good | `src/main/java/cases/GenericWildcardsAndCheckedExceptions.java` | Kotlin output still contains 2 Java-only syntax markers. |

## Interpretation

The evaluator is intentionally structural: it does not claim semantic equivalence. It checks whether conversion preserved file coverage, declaration shape, annotations, method-like declarations, and whether generated Kotlin still contains Java-only syntax or unresolved markers. Files marked `poor` should be manually inspected first.
