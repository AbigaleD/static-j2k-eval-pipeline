# Kotlin Compile Smoke Test

- Status: `failed`
- Kotlin files considered: 3
- Errors: 23
- Warnings: 0
- Message: Generated Kotlin did not compile in a standalone smoke test. This is recorded as a diagnostic signal only; it does not fail the pipeline.

## First Diagnostics

```text
converted/picocli-real-j2k/picocli/AutoComplete.kt:33:16: error: unresolved reference 'CommandLine'.
import picocli.CommandLine.Command
               ^^^^^^^^^^^
converted/picocli-real-j2k/picocli/AutoComplete.kt:34:16: error: unresolved reference 'CommandLine'.
import picocli.CommandLine.HelpCommand
               ^^^^^^^^^^^
converted/picocli-real-j2k/picocli/AutoComplete.kt:35:16: error: unresolved reference 'CommandLine'.
import picocli.CommandLine.IExecutionExceptionHandler
               ^^^^^^^^^^^
converted/picocli-real-j2k/picocli/AutoComplete.kt:36:16: error: unresolved reference 'CommandLine'.
import picocli.CommandLine.IFactory
               ^^^^^^^^^^^
converted/picocli-real-j2k/picocli/AutoComplete.kt:37:16: error: unresolved reference 'CommandLine'.
import picocli.CommandLine.Model.ArgSpec
               ^^^^^^^^^^^
converted/picocli-real-j2k/picocli/AutoComplete.kt:38:16: error: unresolved reference 'CommandLine'.
import picocli.CommandLine.Model.CommandSpec
               ^^^^^^^^^^^
converted/picocli-real-j2k/picocli/AutoComplete.kt:39:16: error: unresolved reference 'CommandLine'.
import picocli.CommandLine.Model.OptionSpec
               ^^^^^^^^^^^
converted/picocli-real-j2k/picocli/AutoComplete.kt:40:16: error: unresolved reference 'CommandLine'.
import picocli.CommandLine.Model.PositionalParamSpec
               ^^^^^^^^^^^
converted/picocli-real-j2k/picocli/AutoComplete.kt:41:16: error: unresolved reference 'CommandLine'.
import picocli.CommandLine.Option
               ^^^^^^^^^^^
converted/picocli-real-j2k/picocli/AutoComplete.kt:42:16: error: unresolved reference 'CommandLine'.
import picocli.CommandLine.Parameters
               ^^^^^^^^^^^
converted/picocli-real-j2k/picocli/AutoComplete.kt:43:16: error: unresolved reference 'CommandLine'.
import picocli.CommandLine.ParseResult
               ^^^^^^^^^^^
converted/picocli-real-j2k/picocli/AutoComplete.kt:44:16: error: unresolved reference 'CommandLine'.
import picocli.CommandLine.Spec
               ^^^^^^^^^^^
converted/picocli-real-j2k/picocli/AutoComplete.kt:1156:1: error: syntax error: Unclosed comment.

^
converted/picocli-real-j2k/picocli/CommandLine.kt:41:16: error: unresolved reference 'CommandLine'.
import picocli.CommandLine.Help.Ansi.IStyle
               ^^^^^^^^^^^
converted/picocli-real-j2k/picocli/CommandLine.kt:42:16: error: unresolved reference 'CommandLine'.
import picocli.CommandLine.Help.Ansi.Style
               ^^^^^^^^^^^
converted/picocli-real-j2k/picocli/CommandLine.kt:43:16: error: unresolved reference 'CommandLine'.
import picocli.CommandLine.Help.Ansi.Text
               ^^^^^^^^^^^
converted/picocli-real-j2k/picocli/CommandLine.kt:44:16: error: unresolved reference 'CommandLine'.
import picocli.CommandLine.Model.*
               ^^^^^^^^^^^
converted/picocli-real-j2k/picocli/CommandLine.kt:45:16: error: unresolved reference 'CommandLine'.
import picocli.CommandLine.ParseResult.GroupMatchContainer
               ^^^^^^^^^^^
converted/picocli-real-j2k/picocli/CommandLine.kt:48:16: error: unresolved reference 'CommandLine'.
import picocli.CommandLine.Help.Column.Overflow.SPAN
               ^^^^^^^^^^^
converted/picocli-real-j2k/picocli/CommandLine.kt:49:16: error: unresolved reference 'CommandLine'.
import picocli.CommandLine.Help.Column.Overflow.TRUNCATE
               ^^^^^^^^^^^
converted/picocli-real-j2k/picocli/CommandLine.kt:50:16: error: unresolved reference 'CommandLine'.
import picocli.CommandLine.Help.Column.Overflow.WRAP
               ^^^^^^^^^^^
converted/picocli-real-j2k/picocli/CommandLine.kt:18047:1: error: syntax error: Unclosed comment.

^
converted/picocli-real-j2k/picocli/package-info.kt:3:1: error: syntax error: Unclosed comment.

^
```
