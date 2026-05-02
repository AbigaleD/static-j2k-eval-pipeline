# Static J2K Evaluation Summary

- Source project: `benchmark/picocli`
- Converted project: `converted/picocli`
- Converter mode: `static-fallback-translator`
- Java files considered: 417
- Kotlin files produced: 417
- Matched Java/Kotlin pairs: 417
- File coverage: 100%
- Average quality score: 64/100
- Average suspiciousness: 36/100
- Status counts: good=101, needs-review=136, poor=180
- Source hash: `8921722cbe186424`
- Converted hash: `38e32f26720d8cc1`

## Preservation Metrics

| Metric | Java source | Kotlin output |
|---|---:|---:|
| Classes | 2634 | 2653 |
| Interfaces | 97 | 97 |
| Enums | 38 | 38 |
| Methods/functions | 14520 | 8886 |
| Annotations | 8669 | 8668 |
| Anonymous classes/signals | 256 | 184 |

## Marker Metrics

- Java-only syntax markers remaining in Kotlin: 17225
- TODO/ERROR/UnsupportedOperationException markers: 124

## Top 10 Most Suspicious Files

| Suspiciousness | Score | Status | File | Primary finding |
|---:|---:|---|---|---|
| 100 | 0 | poor | `picocli-codegen/src/main/java/picocli/codegen/annotation/processing/AbstractCommandSpecProcessor.java` | Method-like declaration count dropped by 9. |
| 100 | 0 | poor | `picocli-tests-java567/src/test/java/picocli/AutoCompleteSystemExitTest.java` | Method-like declaration count dropped by 49. |
| 100 | 0 | poor | `picocli-tests-java9plus/src/test/java/picocli/AutoCompleteSystemExitTest.java` | Method-like declaration count dropped by 35. |
| 100 | 0 | poor | `src/main/java/picocli/CommandLine.java` | Method-like declaration count dropped by 73. |
| 100 | 0 | poor | `src/test/java/picocli/ArgGroupTest.java` | Method-like declaration count dropped by 398. |
| 100 | 0 | poor | `src/test/java/picocli/CommandLineTest.java` | Method-like declaration count dropped by 537. |
| 100 | 0 | poor | `src/test/java/picocli/InterpolatedModelTest.java` | Method-like declaration count dropped by 22. |
| 100 | 0 | poor | `src/test/java/picocli/ModelCommandSpecTest.java` | Method-like declaration count dropped by 124. |
| 98 | 2 | poor | `picocli-codegen-tests-java9plus/src/main/java/picocli/annotation/processing/tests/AnnotatedCommandSourceGenerator.java` | Method-like declaration count dropped by 9. |
| 97 | 3 | poor | `picocli-codegen/src/main/java/picocli/codegen/docgen/manpage/ManPageGenerator.java` | Method-like declaration count dropped by 15. |

## Interpretation

The evaluator is intentionally structural: it does not claim semantic equivalence. It checks whether conversion preserved file coverage, declaration shape, annotations, method-like declarations, and whether generated Kotlin still contains Java-only syntax or unresolved markers. Files marked `poor` should be manually inspected first.
