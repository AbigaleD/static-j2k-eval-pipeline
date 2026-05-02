# Project Summary

This repository evaluates static Java-to-Kotlin conversion on a real Java codebase. The primary benchmark is `picocli`, a mature command-line parsing library with nested classes, annotations, generics, annotation processors, Java modules, and many examples.

## Pipeline

The GitHub Action in `.github/workflows/j2k-eval.yml` performs these steps:

1. Checks out this solution repository.
2. Installs JDK 21.
3. Checks out picocli into `benchmark/picocli`; by default this is `remkop/picocli`, but the GitHub repository variable `BENCHMARK_REPOSITORY` can point to a personal fork.
4. Runs `scripts/run-j2k.sh` to convert Java files into `converted/picocli`.
5. Runs the Kotlin-only evaluator from `evaluator/src/main/kotlin/J2kEvaluator.kt`.
6. Runs the same conversion/evaluation on the custom edge-case dataset.
7. Runs the SAM post-processor (`postprocessor/src/main/kotlin/SamPostProcessor.kt`) on the edge-case output to apply the identified J2K fix.
8. Re-evaluates the post-processed edge cases, producing a before/after quality comparison.
9. Uploads `results/` as a GitHub Actions artifact.

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

Each generated summary records the converter mode from `converted/<target>/conversion-mode.txt`, so fallback runs and real J2K wrapper runs are distinguishable.

## Real J2K Wrapper Results

I implemented an experimental headless wrapper in `j2k-wrapper/src/main/java/StaticJ2kCli.java`. It initializes a minimal IntelliJ PSI environment and invokes `org.jetbrains.kotlin.j2k.OldJavaToKotlinConverter` from the bundled Kotlin plugin.

Current real-wrapper outputs:

| Target | Files | Coverage | Average score | Status counts |
|---|---:|---:|---:|---|
| `benchmark/picocli` | 417 | 99% | 83/100 | good=277, missing=5, needs-review=69, poor=66 |
| `benchmark/picocli/src/main/java` | 3 | 100% | 34/100 | needs-review=1, poor=2 |
| `edge-cases/src/main/java` | 8 | 100% | 91/100 | good=6, needs-review=2 |

The full picocli run produced 412 Kotlin files from 417 Java files. Five wrapper conversions failed and are recorded in `results/picocli-real-j2k-full/conversion-errors.log`; the evaluator reports these as missing files. The standalone Kotlin compile smoke test also failed, which is expected for this experimental file-by-file wrapper without a full project classpath and formatter/postprocessor pass.

The picocli result is intentionally severe: the converter produced broad coverage, but the structural evaluator found large declaration drops and unresolved markers in files such as `src/main/java/picocli/CommandLine.java`, which is exactly the kind of high-priority review signal this pipeline is designed to surface.

## Current Benchmark Choice

I chose picocli instead of spring-petclinic because it is widely used and stresses language conversion more directly: it has annotation-heavy APIs, nested types, extensive generics, command model reflection, Java 9 module descriptors, and several subprojects.

## Converter Boundary

JetBrains J2K is primarily exposed through IntelliJ/Kotlin plugin internals, not as a stable public command-line executable. The pipeline therefore supports an explicit `J2K_CLI_JAR` adapter for the real static converter, an experimental `J2K_CLI_CMD` command wrapper, and a clearly marked fallback translator for reproducible CI smoke tests.

The fallback is not presented as JetBrains J2K quality. It exists so the evaluator, reports, and artifacts can be exercised without a private/internal wrapper.
