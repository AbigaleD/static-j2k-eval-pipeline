# Static J2K Evaluation Summary

- Source project: `edge-cases`
- Converted project: `converted/edge-cases`
- Java files considered: 4
- Kotlin files produced: 4
- Matched Java/Kotlin pairs: 4
- Average heuristic score: 75/100
- Status counts: needs-review=3, poor=1
- Source hash: `ecad5bca4b102ed9`
- Converted hash: `70c583dc4a3c5db2`

## Lowest Scoring Files

| Score | Status | File | Primary finding |
|---:|---|---|---|
| 62 | poor | `src/main/java/cases/NestedAnonymousClasses.java` | Kotlin output still contains 8 Java syntax markers. |
| 76 | needs-review | `src/main/java/cases/AnnotationDenseCommand.java` | Kotlin output still contains 6 Java syntax markers. |
| 80 | needs-review | `src/main/java/cases/GenericWildcardsAndCheckedExceptions.java` | Kotlin output still contains 5 Java syntax markers. |
| 80 | needs-review | `src/main/java/cases/OverloadsAndVarargs.java` | Kotlin output still contains 5 Java syntax markers. |

## Interpretation

The evaluator is intentionally structural: it does not claim semantic equivalence. It checks whether conversion preserved file coverage, declaration shape, annotations, method-like declarations, and whether generated Kotlin still contains Java-only syntax or unresolved markers. Files marked `poor` should be manually inspected first.
