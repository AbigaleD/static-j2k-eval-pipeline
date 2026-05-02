# Agentic Java2K Eval Pipeline

This repository contains a reproducible evaluation pipeline for Java-to-Kotlin conversion. The primary benchmark is [picocli](https://github.com/remkop/picocli), a well-known open-source Java CLI framework chosen instead of spring-petclinic because it is annotation-heavy, generic-heavy, and rich in nested Java language constructs.

## Current Results

The checked-in results include both reproducible fallback runs and experimental real IntelliJ/Kotlin-plugin J2K runs. The real wrapper is driven through IntelliJ internal APIs, so it is version-sensitive, but it exercises the actual static J2K converter rather than the fallback translator.

| Target | Source files | Converter mode | Result |
|---|---:|---|---|
| picocli full benchmark | 417 | `jetbrains-j2k-wrapper` | 99% file coverage (412/417), 83/100 average score, compile smoke failed |
| picocli benchmark | 417 | `static-fallback-translator` | 100% file coverage, 64/100 average score |
| picocli main source | 3 | `jetbrains-j2k-wrapper` | 100% file coverage, 34/100 average score |
| custom edge cases | 8 | `static-fallback-translator` | 100% file coverage, 76/100 average score |
| custom edge cases | 8 | `jetbrains-j2k-wrapper` | 100% file coverage, 91/100 average score |
| edge cases after SAM post-processing | 8 | `static-fallback-translator+sam-postprocessed` | 100% file coverage, 76/100 average score |

Key artifacts:

- `results/picocli/summary.md`
- `results/picocli-real-j2k-full/summary.md`
- `results/picocli-real-j2k-full/compile-smoke.md`
- `results/picocli-real-j2k-full/conversion-errors.log`
- `results/picocli-real-j2k/summary.md`
- `results/edge-cases/summary.md`
- `results/edge-cases-real-j2k/summary.md`
- `results/edge-cases-real-j2k/compile-smoke.md`
- `results/edge-cases-postprocessed/summary.md`
- `results/edge-cases/postprocessed/sam-fixes.md`

## What Is Included

- `.github/workflows/j2k-eval.yml`: GitHub Action pipeline.
- `scripts/run-j2k.sh`: conversion entrypoint.
- `scripts/run-evaluator.sh`: downloads Kotlin compiler, compiles and runs the evaluator.
- `scripts/run-postprocessor.sh`: compiles and runs the SAM post-processor.
- `evaluator/src/main/kotlin/J2kEvaluator.kt`: Kotlin-only structural evaluation logic.
- `postprocessor/src/main/kotlin/SamPostProcessor.kt`: Kotlin post-processor that rewrites verbose `object : Interface` expressions to SAM lambdas, implementing the identified J2K fix.
- `edge-cases/`: custom Java stress-test dataset (8 files covering known J2K weak spots).
- `docs/SUMMARY.md`: pipeline and benchmark summary.
- `docs/HYPOTHESES.md`: 10 conversion hypotheses with failure signals.
- `docs/EDGE_CASE_REPORT.md`: hypotheses, pass/fail conditions, and the implemented Kotlin fix.

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

This repository also includes an experimental headless wrapper around IntelliJ/Kotlin plugin internals:

```bash
export J2K_IDEA_HOME="/Applications/IntelliJ IDEA CE.app/Contents"
export JDK21_HOME=/absolute/path/to/jdk-21
export J2K_CLI_CMD="$PWD/scripts/run-intellij-j2k-wrapper.sh"

bash scripts/run-j2k.sh benchmark/picocli/src/main/java converted/picocli-real-j2k
J2K_COMPILE_SMOKE=1 bash scripts/run-evaluator.sh benchmark/picocli/src/main/java converted/picocli-real-j2k results/picocli-real-j2k

J2K_CONTINUE_ON_ERROR=1 bash scripts/run-j2k.sh benchmark/picocli converted/picocli-real-j2k-full
J2K_COMPILE_SMOKE=1 bash scripts/run-evaluator.sh benchmark/picocli converted/picocli-real-j2k-full results/picocli-real-j2k-full

bash scripts/run-j2k.sh edge-cases/src/main/java converted/edge-cases-real-j2k
J2K_COMPILE_SMOKE=1 bash scripts/run-evaluator.sh edge-cases/src/main/java converted/edge-cases-real-j2k results/edge-cases-real-j2k
```

The pipeline is wired to real static J2K through `J2K_CLI_JAR` or `J2K_CLI_CMD`; the fallback translator exists only so public CI can exercise the evaluator and artifact paths when no internal wrapper is available. The checked-in results under `results/picocli-real-j2k-full/`, `results/picocli-real-j2k/`, and `results/edge-cases-real-j2k/` were produced with a real `jetbrains-j2k-wrapper` run and reflect genuine static J2K conversion quality. Fallback results in `results/picocli/` and `results/edge-cases/` are CI smoke-test artifacts only and must not be interpreted as JetBrains J2K quality.

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

# Apply SAM post-processor and re-evaluate to see before/after improvement
bash scripts/run-postprocessor.sh \
  converted/edge-cases \
  converted/edge-cases-postprocessed \
  results/edge-cases/postprocessed
bash scripts/test-postprocessor.sh
bash scripts/run-evaluator.sh \
  edge-cases \
  converted/edge-cases-postprocessed \
  results/edge-cases-postprocessed
```

`scripts/run-evaluator.sh` downloads the Kotlin compiler into `.tools/` on first run, compiles the evaluator, and writes:

- `results/picocli/summary.md`
- `results/picocli/metrics.csv`
- `results/picocli/metrics.json`
- `results/edge-cases/summary.md`
- `results/edge-cases/metrics.csv`
- `results/edge-cases/metrics.json`
- `results/edge-cases-postprocessed/summary.md`
- `results/edge-cases-postprocessed/metrics.csv`
- `results/edge-cases-postprocessed/metrics.json`
- `results/edge-cases/postprocessed/sam-fixes.md`

## Evaluation Heuristics

The Kotlin evaluator compares the Java source tree against the generated Kotlin tree. It checks:

- source file coverage;
- class/interface/enum declaration preservation;
- method-like declaration preservation;
- annotation preservation;
- anonymous-class versus lambda/object-expression signals;
- remaining Java-only syntax markers in Kotlin output;
- unresolved converter markers such as `TODO()` and `ERROR`.
- optional standalone Kotlin compile smoke status, written to `compile-smoke.md` and `compile-smoke.json` when `J2K_COMPILE_SMOKE=1`.

The score is a triage signal, not a proof of semantic equivalence. Low-scoring files should be reviewed first.

## GitHub Actions

The workflow runs on pushes to `main` and on manual dispatch. It checks out picocli, converts it, evaluates it, runs the edge-case dataset, and uploads `results/` as the `j2k-evaluation-results` artifact.

To run against a personal fork of picocli, set the GitHub repository variable `BENCHMARK_REPOSITORY` to the fork URL, for example:

```text
https://github.com/<your-user>/picocli.git
```

If the variable is not set, the workflow uses upstream `https://github.com/remkop/picocli.git`.

After pushing this repository to GitHub, the public repository URL is the required submission link.
