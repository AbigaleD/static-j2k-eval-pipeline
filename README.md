# Agentic Java2K Eval Pipeline

This repository contains a reproducible evaluation pipeline for Java-to-Kotlin conversion. The primary benchmark is [picocli](https://github.com/remkop/picocli), a well-known open-source Java CLI framework chosen instead of spring-petclinic because it is annotation-heavy, generic-heavy, and rich in nested Java language constructs.

## What Is Included

- `.github/workflows/j2k-eval.yml`: GitHub Action pipeline.
- `scripts/run-j2k.sh`: conversion entrypoint.
- `evaluator/src/main/kotlin/J2kEvaluator.kt`: Kotlin-only evaluation logic.
- `edge-cases/`: custom Java stress-test dataset.
- `docs/SUMMARY.md`: pipeline and benchmark summary.
- `docs/EDGE_CASE_REPORT.md`: hypotheses, expected failures, and a Kotlin remediation pattern.

## Converter Setup

JetBrains static J2K is implemented inside IntelliJ/Kotlin plugin internals rather than as a stable public CLI. For a real converter run, provide a wrapper jar with this interface:

```bash
java -jar "$J2K_CLI_JAR" input.java output.kt
```

Then run:

```bash
export J2K_CLI_JAR=/absolute/path/to/your/static-j2k-wrapper.jar
bash scripts/run-j2k.sh benchmark/picocli converted/picocli
```

If `J2K_CLI_JAR` is not set, `scripts/run-j2k.sh` uses a clearly marked static fallback translator. The fallback exists to keep the CI pipeline, evaluator, and artifacts reproducible; it should not be interpreted as JetBrains J2K conversion quality.

## Reproduce Locally

From the repository root:

```bash
rm -rf benchmark/picocli converted results
mkdir -p benchmark
git clone --depth 1 https://github.com/remkop/picocli.git benchmark/picocli

bash scripts/run-j2k.sh benchmark/picocli converted/picocli
bash scripts/run-evaluator.sh benchmark/picocli converted/picocli results/picocli

bash scripts/run-j2k.sh edge-cases converted/edge-cases
bash scripts/run-evaluator.sh edge-cases converted/edge-cases results/edge-cases
```

`scripts/run-evaluator.sh` downloads the Kotlin compiler into `.tools/` on first run, compiles the evaluator, and writes:

- `results/picocli/summary.md`
- `results/picocli/metrics.csv`
- `results/picocli/metrics.json`
- `results/edge-cases/summary.md`
- `results/edge-cases/metrics.csv`
- `results/edge-cases/metrics.json`

## Evaluation Heuristics

The Kotlin evaluator compares the Java source tree against the generated Kotlin tree. It checks:

- source file coverage;
- class/interface/enum declaration preservation;
- method-like declaration preservation;
- annotation preservation;
- anonymous-class versus lambda/object-expression signals;
- remaining Java-only syntax markers in Kotlin output;
- unresolved converter markers such as `TODO()` and `ERROR`.

The score is a triage signal, not a proof of semantic equivalence. Low-scoring files should be reviewed first.

## GitHub Actions

The workflow runs on pushes to `main` and on manual dispatch. It checks out picocli, converts it, evaluates it, runs the edge-case dataset, and uploads `results/` as the `j2k-evaluation-results` artifact.

After pushing this repository to GitHub, the public repository URL is the required submission link.
