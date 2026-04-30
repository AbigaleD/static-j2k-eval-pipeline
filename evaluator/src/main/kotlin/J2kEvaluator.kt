import java.io.File
import java.security.MessageDigest
import kotlin.math.max
import kotlin.math.roundToInt

data class SourceFileMetrics(
    val path: String,
    val lines: Int,
    val classes: Int,
    val methods: Int,
    val annotations: Int,
    val lambdas: Int,
    val anonymousClasses: Int,
    val rawJavaTokens: Int,
    val nullabilitySignals: Int,
    val unresolvedMarkers: Int,
)

data class PairResult(
    val relativePath: String,
    val source: SourceFileMetrics,
    val converted: SourceFileMetrics?,
    val status: String,
    val score: Int,
    val findings: List<String>,
)

fun main(args: Array<String>) {
    require(args.size == 3) {
        "Usage: J2kEvaluator <java-source-dir> <converted-kotlin-dir> <results-dir>"
    }

    val sourceRoot = File(args[0]).canonicalFile
    val convertedRoot = File(args[1]).canonicalFile
    val resultsRoot = File(args[2]).canonicalFile
    resultsRoot.mkdirs()

    val javaFiles = sourceRoot.walkTopDown()
        .filter { it.isFile && it.extension == "java" }
        .sortedBy { it.relativeTo(sourceRoot).invariantSeparatorsPath }
        .toList()

    val pairResults = javaFiles.map { javaFile ->
        val rel = javaFile.relativeTo(sourceRoot).invariantSeparatorsPath
        val ktRel = rel.removeSuffix(".java") + ".kt"
        val ktFile = File(convertedRoot, ktRel)
        evaluatePair(rel, javaFile, ktFile.takeIf { it.isFile })
    }

    val convertedFiles = convertedRoot.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
    val summary = buildSummary(args[0], args[1], sourceRoot, convertedRoot, pairResults, convertedFiles)
    File(resultsRoot, "summary.md").writeText(summary)
    File(resultsRoot, "metrics.csv").writeText(buildCsv(pairResults))
    File(resultsRoot, "metrics.json").writeText(buildJson(pairResults))

    println(summary)
}

fun evaluatePair(relativePath: String, javaFile: File, ktFile: File?): PairResult {
    val source = metrics(relativePath, javaFile.readText())
    if (ktFile == null) {
        return PairResult(relativePath, source, null, "missing", 0, listOf("No matching Kotlin file was produced."))
    }

    val ktMetrics = metrics(relativePath.removeSuffix(".java") + ".kt", ktFile.readText())
    val findings = mutableListOf<String>()
    var score = 100

    val lineRatio = ktMetrics.lines.toDouble() / max(1, source.lines)
    when {
        lineRatio < 0.20 -> {
            score -= 30
            findings += "Converted file is suspiciously short (${percent(lineRatio)} of Java line count)."
        }
        lineRatio > 2.50 -> {
            score -= 15
            findings += "Converted file is much longer than the source (${percent(lineRatio)} of Java line count)."
        }
    }

    val classDelta = source.classes - ktMetrics.classes
    if (classDelta > 0) {
        score -= classDelta.coerceAtMost(5) * 8
        findings += "Class/interface/enum count dropped by $classDelta."
    }

    val methodDelta = source.methods - ktMetrics.methods
    if (methodDelta > 2) {
        score -= methodDelta.coerceAtMost(10) * 3
        findings += "Method-like declaration count dropped by $methodDelta."
    }

    if (ktMetrics.rawJavaTokens > 0) {
        score -= (ktMetrics.rawJavaTokens * 4).coerceAtMost(30)
        findings += "Kotlin output still contains ${ktMetrics.rawJavaTokens} Java syntax markers."
    }
    if (ktMetrics.unresolvedMarkers > 0) {
        score -= (ktMetrics.unresolvedMarkers * 10).coerceAtMost(40)
        findings += "Output contains ${ktMetrics.unresolvedMarkers} unresolved/error markers."
    }
    if (source.anonymousClasses > 0 && ktMetrics.lambdas == 0) {
        score -= 8
        findings += "Anonymous classes were present but no Kotlin lambda/object-expression signal was detected."
    }
    if (source.annotations > 0 && ktMetrics.annotations < source.annotations / 2) {
        score -= 8
        findings += "Annotation count dropped sharply, which may break framework behavior."
    }

    val status = when {
        score >= 85 -> "good"
        score >= 65 -> "needs-review"
        else -> "poor"
    }
    return PairResult(relativePath, source, ktMetrics, status, score.coerceIn(0, 100), findings.ifEmpty { listOf("No heuristic issues detected.") })
}

fun metrics(path: String, text: String): SourceFileMetrics {
    val noComments = text
        .replace(Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL), "")
        .replace(Regex("//.*"), "")
    return SourceFileMetrics(
        path = path,
        lines = text.lines().count { it.isNotBlank() },
        classes = Regex("\\b(class|interface|enum|record)\\s+[A-Za-z_][A-Za-z0-9_]*").findAll(noComments).count(),
        methods = Regex("\\b(fun\\s+[A-Za-z_][A-Za-z0-9_]*|[A-Za-z_][A-Za-z0-9_<>, ?\\[\\]]+\\s+[A-Za-z_][A-Za-z0-9_]*\\s*\\()").findAll(noComments).count(),
        annotations = Regex("@[A-Za-z_][A-Za-z0-9_.]*").findAll(noComments).count(),
        lambdas = Regex("(->|=>|\\{\\s*[A-Za-z_, ]+\\s*->)").findAll(noComments).count(),
        anonymousClasses = Regex("new\\s+[A-Za-z_][A-Za-z0-9_<>.]*\\s*\\([^)]*\\)\\s*\\{").findAll(noComments).count(),
        rawJavaTokens = listOf(";", "new ", "public ", "private ", "protected ", "static ", "void ", "throws ").sumOf { token ->
            Regex(Regex.escape(token)).findAll(noComments).count()
        },
        nullabilitySignals = Regex("[!?]|\\bOptional\\b|\\bObjects\\.requireNonNull\\b").findAll(noComments).count(),
        unresolvedMarkers = listOf("TODO()", "ERROR", "<caret>", "/*__").sumOf { marker ->
            Regex(Regex.escape(marker)).findAll(text).count()
        },
    )
}

fun buildSummary(
    sourceLabel: String,
    convertedLabel: String,
    sourceRoot: File,
    convertedRoot: File,
    results: List<PairResult>,
    convertedFiles: List<File>,
): String {
    val total = results.size
    val present = results.count { it.converted != null }
    val average = if (results.isEmpty()) 0 else results.map { it.score }.average().roundToInt()
    val statuses = results.groupingBy { it.status }.eachCount().toSortedMap()
    val risky = results.sortedWith(compareBy<PairResult> { it.score }.thenBy { it.relativePath }).take(15)
    val sourceHash = stableDirectoryHash(sourceRoot)
    val convertedHash = stableDirectoryHash(convertedRoot)

    return buildString {
        appendLine("# Static J2K Evaluation Summary")
        appendLine()
        appendLine("- Source project: `$sourceLabel`")
        appendLine("- Converted project: `$convertedLabel`")
        appendLine("- Java files considered: $total")
        appendLine("- Kotlin files produced: ${convertedFiles.size}")
        appendLine("- Matched Java/Kotlin pairs: $present")
        appendLine("- Average heuristic score: $average/100")
        appendLine("- Status counts: ${statuses.entries.joinToString { "${it.key}=${it.value}" }}")
        appendLine("- Source hash: `$sourceHash`")
        appendLine("- Converted hash: `$convertedHash`")
        appendLine()
        appendLine("## Lowest Scoring Files")
        appendLine()
        appendLine("| Score | Status | File | Primary finding |")
        appendLine("|---:|---|---|---|")
        risky.forEach {
            appendLine("| ${it.score} | ${it.status} | `${it.relativePath}` | ${it.findings.first().escapeTable()} |")
        }
        appendLine()
        appendLine("## Interpretation")
        appendLine()
        appendLine("The evaluator is intentionally structural: it does not claim semantic equivalence. It checks whether conversion preserved file coverage, declaration shape, annotations, method-like declarations, and whether generated Kotlin still contains Java-only syntax or unresolved markers. Files marked `poor` should be manually inspected first.")
    }
}

fun buildCsv(results: List<PairResult>): String = buildString {
    appendLine("file,status,score,java_lines,kotlin_lines,java_classes,kotlin_classes,java_methods,kotlin_methods,findings")
    results.forEach {
        appendLine(listOf(
            it.relativePath,
            it.status,
            it.score.toString(),
            it.source.lines.toString(),
            it.converted?.lines?.toString() ?: "",
            it.source.classes.toString(),
            it.converted?.classes?.toString() ?: "",
            it.source.methods.toString(),
            it.converted?.methods?.toString() ?: "",
            it.findings.joinToString("; "),
        ).joinToString(",") { cell -> csv(cell) })
    }
}

fun buildJson(results: List<PairResult>): String = buildString {
    appendLine("[")
    results.forEachIndexed { index, result ->
        append("  {")
        append("\"file\":\"${json(result.relativePath)}\",")
        append("\"status\":\"${result.status}\",")
        append("\"score\":${result.score},")
        append("\"findings\":[${result.findings.joinToString(",") { "\"${json(it)}\"" }}]")
        append("}")
        appendLine(if (index == results.lastIndex) "" else ",")
    }
    appendLine("]")
}

fun stableDirectoryHash(root: File): String {
    val digest = MessageDigest.getInstance("SHA-256")
    if (!root.exists()) return "missing"
    root.walkTopDown()
        .filter { it.isFile }
        .sortedBy { it.relativeTo(root).invariantSeparatorsPath }
        .forEach {
            digest.update(it.relativeTo(root).invariantSeparatorsPath.toByteArray())
            digest.update(0)
            digest.update(it.readBytes())
        }
    return digest.digest().joinToString("") { "%02x".format(it) }.take(16)
}

fun percent(value: Double): String = "${(value * 100).roundToInt()}%"
fun String.escapeTable(): String = replace("|", "\\|")
fun csv(value: String): String = "\"" + value.replace("\"", "\"\"") + "\""
fun json(value: String): String = value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
