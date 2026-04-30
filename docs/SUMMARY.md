# Project Summary

This repository evaluates static Java-to-Kotlin conversion on a real Java codebase. The primary benchmark is `picocli`, a mature command-line parsing library with nested classes, annotations, generics, annotation processors, Java modules, and many examples.

## Pipeline

The GitHub Action in `.github/workflows/j2k-eval.yml` performs these steps:

1. Checks out this solution repository.
2. Installs JDK 21.
3. Checks out `remkop/picocli` into `benchmark/picocli`.
4. Runs `scripts/run-j2k.sh` to convert Java files into `converted/picocli`.
5. Runs the Kotlin-only evaluator from `evaluator/src/main/kotlin/J2kEvaluator.kt`.
6. Runs the same conversion/evaluation on the custom edge-case dataset.
7. Uploads `results/` as a GitHub Actions artifact.

## Evaluation Logic

The evaluator is written strictly in Kotlin. It computes structural quality signals rather than claiming semantic equivalence:

- Java file coverage versus generated Kotlin files.
- Declaration preservation for classes, interfaces, enums, records, and method-like members.
- Annotation preservation.
- Anonymous-class and lambda/object-expression signals.
- Remaining Java syntax markers in generated Kotlin.
- Unresolved converter markers such as `TODO()` or `ERROR`.

The output files are:

- `summary.md`: human-readable headline results.
- `metrics.csv`: tabular per-file metrics.
- `metrics.json`: machine-readable per-file findings.

## Current Benchmark Choice

I chose picocli instead of spring-petclinic because it is widely used and stresses language conversion more directly: it has annotation-heavy APIs, nested types, extensive generics, command model reflection, Java 9 module descriptors, and several subprojects.

## Converter Boundary

JetBrains J2K is primarily exposed through IntelliJ/Kotlin plugin internals, not as a stable public command-line executable. The pipeline therefore supports an explicit `J2K_CLI_JAR` adapter for the real static converter and includes a clearly marked fallback translator for reproducible CI smoke tests.

The fallback is not presented as JetBrains J2K quality. It exists so the evaluator, reports, and artifacts can be exercised without a private/internal wrapper.
