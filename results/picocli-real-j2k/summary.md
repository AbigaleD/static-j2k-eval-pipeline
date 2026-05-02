# Static J2K Evaluation Summary

- Source project: `benchmark/picocli/src/main/java`
- Converted project: `converted/picocli-real-j2k`
- Converter mode: `jetbrains-j2k-wrapper`
- Java files considered: 3
- Kotlin files produced: 3
- Matched Java/Kotlin pairs: 3
- File coverage: 100%
- Average quality score: 34/100
- Average suspiciousness: 66/100
- Status counts: needs-review=1, poor=2
- Source hash: `2cd11870fd266c43`
- Converted hash: `2af955d5df406fda`

## Preservation Metrics

| Metric | Java source | Kotlin output |
|---|---:|---:|
| Classes | 142 | 10 |
| Interfaces | 46 | 2 |
| Enums | 10 | 0 |
| Methods/functions | 2889 | 176 |
| Annotations | 208 | 16 |
| Anonymous classes/signals | 36 | 2 |

## Marker Metrics

- Java-only syntax markers remaining in Kotlin: 99
- TODO/ERROR/UnsupportedOperationException markers: 20

## Top 10 Most Suspicious Files

| Suspiciousness | Score | Status | File | Primary finding |
|---:|---:|---|---|---|
| 100 | 0 | poor | `picocli/CommandLine.java` | Class/interface/enum/record declaration count dropped by 185. |
| 67 | 33 | poor | `picocli/AutoComplete.java` | Class/interface/enum/record declaration count dropped by 1. |
| 30 | 70 | needs-review | `picocli/package-info.java` | Converted file is suspiciously short (7% of Java line count). |

## Interpretation

The evaluator is intentionally structural: it does not claim semantic equivalence. It checks whether conversion preserved file coverage, declaration shape, annotations, method-like declarations, and whether generated Kotlin still contains Java-only syntax or unresolved markers. Files marked `poor` should be manually inspected first.

## Kotlin Compile Smoke Test

- Status: `failed`
- Kotlin files considered: 3
- Errors: 23
- Warnings: 0
- Diagnostics: `compile-smoke.md`
