# Limitations

## Converter Mode

The default fallback mode does not measure JetBrains static J2K quality.

`scripts/run-j2k.sh` invokes a real IntelliJ/Kotlin-plugin based converter when either `J2K_CLI_JAR` or `J2K_CLI_CMD` is set. Without those variables, the script uses a small text-based fallback translator so the pipeline, evaluator, and artifacts remain reproducible in public CI.

Fallback results are useful for validating repository wiring and evaluator output formats. They must not be interpreted as results from JetBrains static J2K.

## Experimental IntelliJ Wrapper

`j2k-wrapper/src/main/java/StaticJ2kCli.java` is a proof-of-concept wrapper around IntelliJ/Kotlin plugin internals. It is closer to the assignment's static J2K requirement than manual IDE conversion, but it has important limitations:

- it depends on IntelliJ IDEA and Kotlin plugin internal APIs;
- it currently builds a minimal PSI runtime with no-op IntelliJ services;
- it is version-sensitive and tested locally against IDEA 2025.2.6.2 with JDK 21;
- it converts files independently rather than building a full IntelliJ project model;
- it does not run the IDE formatter or full J2K post-processing pipeline.

For the full picocli run, `J2K_CONTINUE_ON_ERROR=1` was used so that isolated wrapper failures are recorded without discarding the rest of the benchmark. This produced 412 Kotlin files from 417 Java files.

Manual IntelliJ IDEA conversion can be useful for visual inspection, but it is not treated as a submission result because it is not automated in GitHub Actions.

## Evaluation Scope

The current evaluator uses regex/text heuristics. These are triage signals, not proof of semantic equivalence.

The evaluator can flag likely issues such as missing files, dropped declarations, reduced annotation counts, remaining Java syntax, and unresolved markers. It does not prove that converted Kotlin:

- compiles;
- preserves overload resolution;
- preserves framework/runtime behavior;
- preserves Java interop contracts;
- preserves nullability or exception semantics;
- is idiomatic Kotlin.

The optional compile smoke test is also diagnostic only. It compiles the generated Kotlin tree as a standalone Kotlin input, without reconstructing the original Gradle/Maven dependency graph. A failed smoke test is useful evidence, but it is not equivalent to a project-level Kotlin build.

## Benchmark Scope

The primary benchmark is picocli. This gives realistic Java syntax, annotations, generics, nested types, and module descriptors, but it is still one project. Results should not be generalized to all Java codebases without adding more benchmarks.

## Recommended Interpretation

Use `summary.md`, `metrics.csv`, and `metrics.json` to prioritize manual review. Low-scoring files are candidates for deeper PSI-based analysis, compilation checks, or behavioral tests.
