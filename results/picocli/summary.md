# Static J2K Evaluation Summary

- Source project: `benchmark/picocli`
- Converted project: `converted/picocli`
- Java files considered: 417
- Kotlin files produced: 417
- Matched Java/Kotlin pairs: 417
- File coverage: 100%
- Average quality score: 66/100
- Average suspiciousness: 34/100
- Status counts: good=61, needs-review=271, poor=85
- Source hash: `0ca0646661e356f6`
- Converted hash: `ea0a248c444d0583`

## Preservation Metrics

| Metric | Java source | Kotlin output |
|---|---:|---:|
| Classes | 2634 | 2647 |
| Interfaces | 97 | 97 |
| Enums | 38 | 38 |
| Methods/functions | 14520 | 14752 |
| Annotations | 8669 | 8669 |
| Anonymous classes/signals | 256 | 184 |

## Marker Metrics

- Java-only syntax markers remaining in Kotlin: 26309
- TODO/ERROR/UnsupportedOperationException markers: 124

## Top 10 Most Suspicious Files

| Suspiciousness | Score | Status | File | Primary finding |
|---:|---:|---|---|---|
| 100 | 0 | poor | `picocli-tests-java567/src/test/java/picocli/AutoCompleteSystemExitTest.java` | Kotlin output still contains 225 Java-only syntax markers. |
| 100 | 0 | poor | `src/test/java/picocli/ArgGroupTest.java` | Kotlin output still contains 1081 Java-only syntax markers. |
| 100 | 0 | poor | `src/test/java/picocli/CommandLineTest.java` | Kotlin output still contains 1321 Java-only syntax markers. |
| 100 | 0 | poor | `src/test/java/picocli/ModelCommandSpecTest.java` | Kotlin output still contains 388 Java-only syntax markers. |
| 98 | 2 | poor | `src/main/java/picocli/CommandLine.java` | Kotlin output still contains 5524 Java-only syntax markers. |
| 85 | 15 | poor | `picocli-codegen/src/main/java/picocli/codegen/docgen/manpage/ManPageGenerator.java` | Kotlin output still contains 189 Java-only syntax markers. |
| 85 | 15 | poor | `src/test/java/picocli/ExecuteTest.java` | Kotlin output still contains 643 Java-only syntax markers. |
| 85 | 15 | poor | `src/test/java/picocli/HelpTest.java` | Kotlin output still contains 1316 Java-only syntax markers. |
| 80 | 20 | poor | `picocli-codegen/src/main/java/picocli/codegen/annotation/processing/AbstractCommandSpecProcessor.java` | Kotlin output still contains 183 Java-only syntax markers. |
| 80 | 20 | poor | `picocli-tests-java9plus/src/test/java/picocli/AutoCompleteSystemExitTest.java` | Kotlin output still contains 198 Java-only syntax markers. |

## Interpretation

The evaluator is intentionally structural: it does not claim semantic equivalence. It checks whether conversion preserved file coverage, declaration shape, annotations, method-like declarations, and whether generated Kotlin still contains Java-only syntax or unresolved markers. Files marked `poor` should be manually inspected first.
