# Static J2K Evaluation Summary

- Source project: `benchmark/picocli`
- Converted project: `converted/picocli-real-j2k-full`
- Converter mode: `jetbrains-j2k-wrapper`
- Java files considered: 417
- Kotlin files produced: 412
- Matched Java/Kotlin pairs: 412
- File coverage: 99%
- Average quality score: 83/100
- Average suspiciousness: 17/100
- Status counts: good=277, missing=5, needs-review=69, poor=66
- Source hash: `8921722cbe186424`
- Converted hash: `ae21a925928f2f7d`

## Preservation Metrics

| Metric | Java source | Kotlin output |
|---|---:|---:|
| Classes | 2634 | 2062 |
| Interfaces | 97 | 48 |
| Enums | 38 | 24 |
| Methods/functions | 14520 | 8913 |
| Annotations | 8669 | 10039 |
| Anonymous classes/signals | 256 | 290 |

## Marker Metrics

- Java-only syntax markers remaining in Kotlin: 1606
- TODO/ERROR/UnsupportedOperationException markers: 117

## Top 10 Most Suspicious Files

| Suspiciousness | Score | Status | File | Primary finding |
|---:|---:|---|---|---|
| 100 | 0 | poor | `picocli-codegen/src/main/java/picocli/codegen/docgen/manpage/ManPageGenerator.java` | Class/interface/enum/record declaration count dropped by 7. |
| 100 | 0 | missing | `picocli-spring-boot-starter/src/test/java/picocli/spring/PicocliSpringFactoryTest.java` | No matching Kotlin file was produced. |
| 100 | 0 | poor | `picocli-tests-java567/src/test/java/picocli/AutoCompleteSystemExitTest.java` | Method-like declaration count dropped by 18. |
| 100 | 0 | poor | `picocli-tests-java9plus/src/test/java/picocli/AutoCompleteSystemExitTest.java` | Method-like declaration count dropped by 13. |
| 100 | 0 | poor | `src/main/java/picocli/CommandLine.java` | Class/interface/enum/record declaration count dropped by 185. |
| 100 | 0 | poor | `src/test/java/picocli/ArgGroupTest.java` | Class/interface/enum/record declaration count dropped by 16. |
| 100 | 0 | poor | `src/test/java/picocli/CommandLineTest.java` | Class/interface/enum/record declaration count dropped by 119. |
| 100 | 0 | poor | `src/test/java/picocli/CommandMethodTest.java` | Class/interface/enum/record declaration count dropped by 14. |
| 100 | 0 | missing | `src/test/java/picocli/GenericTest.java` | No matching Kotlin file was produced. |
| 100 | 0 | poor | `src/test/java/picocli/HelpTest.java` | Class/interface/enum/record declaration count dropped by 48. |

## Interpretation

The evaluator is intentionally structural: it does not claim semantic equivalence. It checks whether conversion preserved file coverage, declaration shape, annotations, method-like declarations, and whether generated Kotlin still contains Java-only syntax or unresolved markers. Files marked `poor` should be manually inspected first.

## Kotlin Compile Smoke Test

- Status: `failed`
- Kotlin files considered: 412
- Errors: 1
- Warnings: 0
- Diagnostics: `compile-smoke.md`
