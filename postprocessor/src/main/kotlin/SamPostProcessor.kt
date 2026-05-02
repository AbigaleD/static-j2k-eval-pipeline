import java.io.File

/**
 * Post-processor that rewrites single-method `object : InterfaceName` expressions
 * produced by J2K into idiomatic Kotlin SAM lambdas.
 *
 * Addresses a known J2K limitation: anonymous classes implementing a single abstract
 * method (SAM) are converted to verbose `object` expressions instead of concise lambdas.
 *
 * Input (typical J2K output):
 *   object : Comparator<String> {
 *       override fun compare(left: String, right: String): Int { ... }
 *   }
 *
 * Output (SAM lambda form):
 *   Comparator<String> { left, right ->
 *       ...
 *   }
 *
 * Nested anonymous classes are handled by converting innermost expressions first.
 *
 * Limitations:
 * - Parameter parsing uses comma-splitting with angle-bracket depth tracking; deeply
 *   nested generic types with parentheses inside type arguments are not supported.
 * - String template literals `"${...}"` inside method bodies are not specially tracked
 *   during brace counting; extremely unusual cases may produce malformed output.
 * - Only the primary SAM pattern is targeted; interfaces with default or static members
 *   whose anonymous implementations override more than one method are left unchanged.
 */

data class SamTransformation(
    val file: String,
    val line: Int,
    val interfaceName: String,
    val methodName: String,
)

data class OverrideFunInfo(
    val name: String,
    val paramNames: List<String>,
    val body: String,
)

fun main(args: Array<String>) {
    require(args.size in 2..3) {
        "Usage: SamPostProcessor <input-dir> <output-dir> [results-dir]"
    }
    val inputRoot = File(args[0]).canonicalFile
    val outputRoot = File(args[1]).canonicalFile
    val resultsRoot = if (args.size == 3) File(args[2]).canonicalFile else null

    require(inputRoot.isDirectory) { "Input directory does not exist: $inputRoot" }

    val allTransformations = mutableListOf<SamTransformation>()
    var filesProcessed = 0
    var filesModified = 0

    inputRoot.walkTopDown()
        .filter { it.isFile && it.extension == "kt" }
        .sortedBy { it.relativeTo(inputRoot).invariantSeparatorsPath }
        .forEach { file ->
            val rel = file.relativeTo(inputRoot).invariantSeparatorsPath
            val original = file.readText()
            val (transformed, transforms) = convertSamExpressions(rel, original)
            filesProcessed++
            if (transforms.isNotEmpty()) filesModified++
            allTransformations += transforms

            val outFile = File(outputRoot, rel)
            outFile.parentFile.mkdirs()
            outFile.writeText(transformed)
        }

    writeConversionMode(inputRoot, outputRoot)

    val report = buildReport(allTransformations, filesProcessed, filesModified)
    println(report)

    resultsRoot?.let {
        it.mkdirs()
        File(it, "sam-fixes.md").writeText(report)
    }
}

private val OBJECT_EXPRESSION_PATTERN = Regex(
    """object\s*:\s*([A-Za-z_][A-Za-z0-9_]*(?:<[^>]*>)?)\s*\{"""
)

fun convertSamExpressions(filename: String, text: String): Pair<String, List<SamTransformation>> {
    val transformations = mutableListOf<SamTransformation>()
    var result = text
    // Iteratively convert innermost object expressions first.
    // Each iteration handles one conversion; we repeat until none remain.
    var progress = true
    while (progress) {
        progress = false
        val next = trySingleSamConversion(filename, result, transformations)
        if (next != null) {
            result = next
            progress = true
        }
    }
    return Pair(result, transformations)
}

fun writeConversionMode(inputRoot: File, outputRoot: File) {
    outputRoot.mkdirs()
    val inputModeFile = File(inputRoot, "conversion-mode.txt")
    val sourceMode = inputModeFile
        .takeIf { it.isFile }
        ?.readLines()
        ?.firstOrNull { it.startsWith("mode=") }
        ?.substringAfter("mode=")
        ?.ifBlank { null }
        ?: "unknown"

    File(outputRoot, "conversion-mode.txt").writeText(
        buildString {
            appendLine("mode=$sourceMode+sam-postprocessed")
            appendLine("source=$inputRoot")
            appendLine("output=$outputRoot")
            appendLine("note=Kotlin SAM post-processor applied after initial conversion.")
        },
    )
}

fun trySingleSamConversion(
    filename: String,
    text: String,
    transformations: MutableList<SamTransformation>,
): String? {
    val matches = OBJECT_EXPRESSION_PATTERN.findAll(text).toList()

    // Process rightmost match first — for a flat list of object expressions this gives
    // last-in-source-order; for nested ones this selects the innermost (because the
    // inner match appears after the outer match's opening brace in the string).
    for (match in matches.reversed()) {
        val openBraceIdx = match.range.last
        if (text.getOrNull(openBraceIdx) != '{') continue

        val closeBraceIdx = findMatchingBrace(text, openBraceIdx) ?: continue
        val interfaceName = match.groupValues[1]
        val blockContent = text.substring(openBraceIdx + 1, closeBraceIdx)

        // If this block contains a nested object expression, skip and handle the inner one first.
        if (OBJECT_EXPRESSION_PATTERN.containsMatchIn(blockContent)) continue

        val overrideFun = extractSingleOverrideFun(blockContent) ?: continue

        val lineNum = text.substring(0, match.range.first).count { it == '\n' } + 1
        val linePrefix = text.substring(text.lastIndexOf('\n', match.range.first - 1) + 1, match.range.first)
        val baseIndent = linePrefix.takeIf { it.all(Char::isWhitespace) } ?: ""
        val lambda = buildSamLambda(interfaceName, overrideFun, baseIndent)

        transformations += SamTransformation(filename, lineNum, interfaceName, overrideFun.name)
        return text.substring(0, match.range.first) + lambda + text.substring(closeBraceIdx + 1)
    }
    return null
}

fun findMatchingBrace(text: String, openIdx: Int): Int? {
    require(text.getOrNull(openIdx) == '{') {
        "Expected '{' at index $openIdx, found '${text.getOrNull(openIdx)}'"
    }
    var depth = 1
    var i = openIdx + 1
    while (i < text.length && depth > 0) {
        when (text[i]) {
            '{' -> depth++
            '}' -> depth--
        }
        i++
    }
    return if (depth == 0) i - 1 else null
}

private val OVERRIDE_FUN_HEADER = Regex(
    // Matches: override fun methodName(params): ReturnType {
    // The final '{' is always captured so match.range.last == index of '{'
    """override\s+fun\s+([A-Za-z_][A-Za-z0-9_]*)\s*\(([^)]*)\)(?:\s*:\s*[A-Za-z_][A-Za-z0-9_<>?,.* ]*)?\s*\{"""
)

fun extractSingleOverrideFun(blockContent: String): OverrideFunInfo? {
    val matches = OVERRIDE_FUN_HEADER.findAll(blockContent).toList()
    if (matches.size != 1) return null

    val match = matches[0]
    val name = match.groupValues[1]
    val paramsText = match.groupValues[2].trim()
    val paramNames = parseParamNames(paramsText)

    // match.range.last is the '{' that opens the method body
    val bodyOpenIdx = match.range.last
    if (blockContent.getOrNull(bodyOpenIdx) != '{') return null
    val bodyCloseIdx = findMatchingBrace(blockContent, bodyOpenIdx) ?: return null
    val body = blockContent.substring(bodyOpenIdx + 1, bodyCloseIdx).trimIndent().trim()

    return OverrideFunInfo(name, paramNames, body)
}

fun parseParamNames(paramsText: String): List<String> {
    if (paramsText.isBlank()) return emptyList()
    // Split on commas at angle-bracket depth 0 to handle generic type params.
    val params = mutableListOf<String>()
    var depth = 0
    val current = StringBuilder()
    for (char in paramsText) {
        when {
            char == '<' -> { depth++; current.append(char) }
            char == '>' -> { depth--; current.append(char) }
            char == ',' && depth == 0 -> {
                params += current.toString().trim()
                current.clear()
            }
            else -> current.append(char)
        }
    }
    if (current.isNotBlank()) params += current.toString().trim()
    // Each entry is "name: Type" — extract just the name.
    return params.map { it.substringBefore(':').trim() }
}

fun buildSamLambda(interfaceName: String, overrideFun: OverrideFunInfo, baseIndent: String = ""): String {
    val params = overrideFun.paramNames.joinToString(", ")
    val body = normalizeReturnsForLambda(interfaceName, overrideFun.body)
    val indentedBody = body.lines().joinToString("\n") { line ->
        if (line.isBlank()) line else "$baseIndent    $line"
    }
    return if (params.isEmpty()) {
        "$interfaceName {\n$indentedBody\n$baseIndent}"
    } else {
        "$interfaceName { $params ->\n$indentedBody\n$baseIndent}"
    }
}

fun normalizeReturnsForLambda(interfaceName: String, body: String): String {
    val lines = body.lines()
    val lastReturnIndex = lines.indexOfLast { it.trimStart().startsWith("return") }
    if (lastReturnIndex == -1) return body

    val label = interfaceName
        .substringBefore('<')
        .substringAfterLast('.')
        .filter { it.isLetterOrDigit() || it == '_' }
        .ifBlank { "sam" }

    return lines.mapIndexed { index, line ->
        val indent = line.takeWhile { it.isWhitespace() }
        val trimmed = line.trimStart()
        if (!trimmed.startsWith("return")) {
            line
        } else {
            val expression = trimmed.removePrefix("return").trim().removeSuffix(";").trim()
            when {
                index == lastReturnIndex && expression.isBlank() -> "${indent}Unit"
                index == lastReturnIndex -> indent + expression
                expression.isBlank() -> "${indent}return@$label"
                else -> "${indent}return@$label $expression"
            }
        }
    }.joinToString("\n")
}

fun buildReport(
    transformations: List<SamTransformation>,
    filesProcessed: Int,
    filesModified: Int,
): String = buildString {
    appendLine("# SAM Post-Processor Results")
    appendLine()
    appendLine("| Metric | Value |")
    appendLine("|---|---:|")
    appendLine("| Files processed | $filesProcessed |")
    appendLine("| Files modified  | $filesModified |")
    appendLine("| SAM conversions | ${transformations.size} |")
    appendLine()
    if (transformations.isEmpty()) {
        appendLine("No SAM conversions were applied.")
        appendLine()
        appendLine("> In fallback-converter mode, J2K output retains Java syntax rather than producing")
        appendLine("> `object :` expressions, so there is nothing for this post-processor to rewrite.")
        appendLine("> Run with a real JetBrains J2K wrapper (`J2K_CLI_JAR`) to see conversions.")
    } else {
        appendLine("## Conversions Applied")
        appendLine()
        appendLine("| File | Line | Interface | Method |")
        appendLine("|---|---:|---|---|")
        transformations.forEach {
            appendLine("| `${it.file}` | ${it.line} | `${it.interfaceName}` | `${it.methodName}` |")
        }
    }
    appendLine()
    appendLine("## What This Fix Addresses")
    appendLine()
    appendLine("Static J2K converts `new Interface() { @Override public T method(...) { ... } }`")
    appendLine("to a verbose `object : Interface` expression instead of a Kotlin SAM lambda.")
    appendLine("This post-processor detects the verbose form and rewrites it.")
    appendLine()
    appendLine("**Before** (J2K output):")
    appendLine()
    appendLine("```kotlin")
    appendLine("object : Comparator<String> {")
    appendLine("    override fun compare(left: String, right: String): Int {")
    appendLine("        return left.compareTo(right)")
    appendLine("    }")
    appendLine("}")
    appendLine("```")
    appendLine()
    appendLine("**After** (post-processor output):")
    appendLine()
    appendLine("```kotlin")
    appendLine("Comparator<String> { left, right ->")
    appendLine("    left.compareTo(right)")
    appendLine("}")
    appendLine("```")
    appendLine()
    appendLine("Nested anonymous classes are handled by converting innermost expressions first,")
    appendLine("so deeply nested SAM types are fully simplified in a single pass.")
}
