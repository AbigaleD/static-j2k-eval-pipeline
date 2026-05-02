# Production Upgrade Path

## Harden Real J2K Conversion

This repository now contains an experimental wrapper around the IntelliJ/Kotlin plugin J2K implementation. To make it production-grade, harden it into a stable wrapper rather than relying on the current minimal PSI runtime.

Expected wrapper contract:

```bash
java -jar "$J2K_CLI_JAR" input.java output.kt
```

Recommended steps:

1. Package `j2k-wrapper/src/main/java/StaticJ2kCli.java` as a proper IntelliJ Platform based CLI jar.
2. Initialize a real IntelliJ project model for the checked-out Java repository.
3. Load Java files through PSI rather than plain text.
4. Invoke the Kotlin plugin's J2K converter on each selected `PsiJavaFile`.
5. Run the converter post-processor/formatter.
6. Write each converted Kotlin file to the requested output path.
7. Set `J2K_CLI_JAR` or `J2K_CLI_CMD` in GitHub Actions and local runs.
8. Preserve `J2K_CONTINUE_ON_ERROR=1` for large benchmark sweeps so isolated converter failures can be reported as missing files instead of aborting the whole run.
9. Keep fallback mode only for smoke tests where the real wrapper is unavailable.

The pipeline already has the adapter point in `scripts/run-j2k.sh`; the production change is to use the hardened wrapper in CI.

## Improve Evaluation With PSI

Regex-based declaration matching is fast and portable, but it is approximate. PSI-based matching is better because it works on parsed language structure instead of text patterns.

PSI-based evaluation can:

- distinguish declarations from comments and string literals;
- match Java classes, methods, fields, annotations, and type parameters to Kotlin declarations;
- inspect annotation targets and arguments precisely;
- compare overload sets by signature rather than method-name counts;
- track static members and companion-object mappings;
- inspect wildcard variance and nullability annotations;
- separate syntax preservation from semantic preservation.

## Add Compile And Behavior Checks

After real J2K conversion, add a second evaluation tier:

1. Configure Kotlin in the converted project.
2. Compile generated Kotlin with the original Java dependencies.
3. Run selected picocli tests or focused smoke tests.
4. Record compiler diagnostics in `metrics.json`.
5. Link each diagnostic back to the source Java file and converted Kotlin file.

This would turn the current triage report into a stronger conversion-quality report.
