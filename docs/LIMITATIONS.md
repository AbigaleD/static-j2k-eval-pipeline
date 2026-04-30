# Limitations

## Converter Mode

The default fallback mode does not measure JetBrains static J2K quality.

`scripts/run-j2k.sh` only invokes a real IntelliJ/Kotlin-plugin based converter when `J2K_CLI_JAR` is set. Without that variable, the script uses a small text-based fallback translator so the pipeline, evaluator, and artifacts remain reproducible in public CI.

Fallback results are useful for validating repository wiring and evaluator output formats. They must not be interpreted as results from JetBrains static J2K.

## Evaluation Scope

The current evaluator uses regex/text heuristics. These are triage signals, not proof of semantic equivalence.

The evaluator can flag likely issues such as missing files, dropped declarations, reduced annotation counts, remaining Java syntax, and unresolved markers. It does not prove that converted Kotlin:

- compiles;
- preserves overload resolution;
- preserves framework/runtime behavior;
- preserves Java interop contracts;
- preserves nullability or exception semantics;
- is idiomatic Kotlin.

## Benchmark Scope

The primary benchmark is picocli. This gives realistic Java syntax, annotations, generics, nested types, and module descriptors, but it is still one project. Results should not be generalized to all Java codebases without adding more benchmarks.

## Recommended Interpretation

Use `summary.md`, `metrics.csv`, and `metrics.json` to prioritize manual review. Low-scoring files are candidates for deeper PSI-based analysis, compilation checks, or behavioral tests.
