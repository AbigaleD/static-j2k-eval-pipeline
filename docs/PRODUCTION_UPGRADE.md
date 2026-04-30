# Production Upgrade Path

## Replace Fallback Conversion

To measure real JetBrains static J2K quality, replace fallback conversion with a wrapper around the IntelliJ/Kotlin plugin J2K implementation.

Expected wrapper contract:

```bash
java -jar "$J2K_CLI_JAR" input.java output.kt
```

Recommended steps:

1. Build a small IntelliJ Platform/Kotlin plugin based CLI jar.
2. Initialize an IntelliJ project model for the checked-out Java repository.
3. Load Java files through PSI rather than plain text.
4. Invoke the Kotlin plugin's J2K converter on each selected `PsiJavaFile`.
5. Run the converter post-processor/formatter.
6. Write each converted Kotlin file to the requested output path.
7. Set `J2K_CLI_JAR` in GitHub Actions and local runs.
8. Keep fallback mode only for smoke tests where the real wrapper is unavailable.

The pipeline already has the adapter point in `scripts/run-j2k.sh`; the production change is to provide and configure the real wrapper.

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
