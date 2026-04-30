# Static J2K Evaluation Summary

- Source project: `benchmark/picocli`
- Converted project: `converted/picocli`
- Java files considered: 417
- Kotlin files produced: 417
- Matched Java/Kotlin pairs: 417
- Average heuristic score: 72/100
- Status counts: good=61, needs-review=293, poor=63
- Source hash: `0ca0646661e356f6`
- Converted hash: `ea0a248c444d0583`

## Lowest Scoring Files

| Score | Status | File | Primary finding |
|---:|---|---|---|
| 22 | poor | `picocli-tests-java567/src/test/java/picocli/AutoCompleteSystemExitTest.java` | Kotlin output still contains 227 Java syntax markers. |
| 22 | poor | `src/main/java/picocli/AutoComplete.java` | Kotlin output still contains 277 Java syntax markers. |
| 22 | poor | `src/main/java/picocli/CommandLine.java` | Kotlin output still contains 5525 Java syntax markers. |
| 30 | poor | `picocli-codegen/src/main/java/picocli/codegen/annotation/processing/AbstractCommandSpecProcessor.java` | Kotlin output still contains 183 Java syntax markers. |
| 30 | poor | `picocli-tests-java8/src/test/java/picocli/MapOptionsOptionalTest.java` | Kotlin output still contains 47 Java syntax markers. |
| 30 | poor | `picocli-tests-java9plus/src/test/java/picocli/AutoCompleteSystemExitTest.java` | Kotlin output still contains 200 Java syntax markers. |
| 40 | poor | `picocli-codegen/src/main/java/picocli/codegen/aot/graalvm/processor/AbstractGenerator.java` | Kotlin output still contains 23 Java syntax markers. |
| 40 | poor | `src/test/java/picocli/Issue1383MultipleArgumentsTest.java` | Kotlin output still contains 27 Java syntax markers. |
| 42 | poor | `src/test/java/picocli/ExecuteTest.java` | Kotlin output still contains 643 Java syntax markers. |
| 50 | poor | `picocli-codegen-tests-java9plus/src/test/java/picocli/annotation/processing/tests/AbstractCommandSpecProcessorTest.java` | Kotlin output still contains 68 Java syntax markers. |
| 50 | poor | `picocli-codegen-tests-java9plus/src/test/resources/picocli/examples/logging/LoggingMixin.java` | Kotlin output still contains 21 Java syntax markers. |
| 50 | poor | `picocli-examples/src/main/java/picocli/examples/logging_mixin_advanced/LoggingMixin.java` | Kotlin output still contains 21 Java syntax markers. |
| 52 | poor | `picocli-codegen/src/main/java/picocli/codegen/docgen/manpage/ManPageGenerator.java` | Kotlin output still contains 189 Java syntax markers. |
| 52 | poor | `src/test/java/picocli/HelpTest.java` | Kotlin output still contains 1316 Java syntax markers. |
| 52 | poor | `src/test/java/picocli/ModelCommandSpecTest.java` | Kotlin output still contains 388 Java syntax markers. |

## Interpretation

The evaluator is intentionally structural: it does not claim semantic equivalence. It checks whether conversion preserved file coverage, declaration shape, annotations, method-like declarations, and whether generated Kotlin still contains Java-only syntax or unresolved markers. Files marked `poor` should be manually inspected first.
