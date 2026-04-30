# Static J2K Evaluation Summary

- Source project: `edge-cases`
- Converted project: `converted/edge-cases`
- Java files considered: 4
- Kotlin files produced: 4
- Matched Java/Kotlin pairs: 4
- File coverage: 100%
- Average quality score: 71/100
- Average suspiciousness: 29/100
- Status counts: needs-review=3, poor=1
- Source hash: `ecad5bca4b102ed9`
- Converted hash: `70c583dc4a3c5db2`

## Preservation Metrics

| Metric | Java source | Kotlin output |
|---|---:|---:|
| Classes | 4 | 4 |
| Interfaces | 2 | 2 |
| Enums | 0 | 0 |
| Methods/functions | 15 | 15 |
| Annotations | 9 | 9 |
| Anonymous classes/signals | 2 | 0 |

## Marker Metrics

- Java-only syntax markers remaining in Kotlin: 24
- TODO/ERROR/UnsupportedOperationException markers: 0

## Top 10 Most Suspicious Files

| Suspiciousness | Score | Status | File | Primary finding |
|---:|---:|---|---|---|
| 52 | 48 | poor | `src/main/java/cases/NestedAnonymousClasses.java` | Kotlin output still contains 8 Java-only syntax markers. |
| 24 | 76 | needs-review | `src/main/java/cases/AnnotationDenseCommand.java` | Kotlin output still contains 6 Java-only syntax markers. |
| 20 | 80 | needs-review | `src/main/java/cases/GenericWildcardsAndCheckedExceptions.java` | Kotlin output still contains 5 Java-only syntax markers. |
| 20 | 80 | needs-review | `src/main/java/cases/OverloadsAndVarargs.java` | Kotlin output still contains 5 Java-only syntax markers. |

## Interpretation

The evaluator is intentionally structural: it does not claim semantic equivalence. It checks whether conversion preserved file coverage, declaration shape, annotations, method-like declarations, and whether generated Kotlin still contains Java-only syntax or unresolved markers. Files marked `poor` should be manually inspected first.
