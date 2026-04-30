import java.io.File
import java.security.MessageDigest
import kotlin.math.max
import kotlin.math.roundToInt

data class SourceFileMetrics(
    val path: String,
    val lines: Int,
    val classes: Int,
    val interfaces: Int,
    val enums: Int,
    val records: Int,
    val methods: Int,
    val functions: Int,
    val annotations: Int,
    val lambdas: Int,
    val objectExpressions: Int,
    val anonymousClasses: Int,
    val javaOnlySyntaxMarkers: Int,
    val javaOnlySyntaxDetails: Map<String, Int>,
    val nullabilitySignals: Int,
    val todoErrorUnsupportedMarkers: Int,
    val todoErrorUnsupportedDetails: Map<String, Int>,
)

data class PairResult(
    val relativePath: String,
    val source: SourceFileMetrics,
    val converted: SourceFileMetrics?,
    val status: String,
    val score: Int,
    val suspiciousness: Int,
    val findings: List<String>,
)

data class AggregateMetrics(
    val sourceFiles: Int,
    val convertedFiles: Int,
    val matchedFiles: Int,
    val fileCoveragePercent: Int,
    val averageScore: Int,
    val averageSuspiciousness: Int,
    val sourceClasses: Int,
    val convertedClasses: Int,
    val sourceInterfaces: Int,
    val convertedInterfaces: Int,
    val sourceEnums: Int,
    val convertedEnums: Int,
    val sourceMethods: Int,
    val convertedMethods: Int,
    val sourceAnnotations: Int,
    val convertedAnnotations: Int,
    val javaOnlySyntaxMarkers: Int,
    val todoErrorUnsupportedMarkers: Int,
    val sourceAnonymousClasses: Int,
    val convertedAnonymousSignals: Int,
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
        return PairResult(relativePath, source, null, "missing", 0, 100, listOf("No matching Kotlin file was produced."))
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

    val classDelta = source.classLikeDeclarations - ktMetrics.classLikeDeclarations
    if (classDelta > 0) {
        score -= classDelta.coerceAtMost(5) * 8
        findings += "Class/interface/enum/record declaration count dropped by $classDelta."
    }

    val methodDelta = source.callableDeclarations - ktMetrics.callableDeclarations
    if (methodDelta > 2) {
        score -= methodDelta.coerceAtMost(10) * 3
        findings += "Method-like declaration count dropped by $methodDelta."
    }

    if (ktMetrics.javaOnlySyntaxMarkers > 0) {
        score -= (ktMetrics.javaOnlySyntaxMarkers * 4).coerceAtMost(35)
        findings += "Kotlin output still contains ${ktMetrics.javaOnlySyntaxMarkers} Java-only syntax markers."
    }
    if (ktMetrics.todoErrorUnsupportedMarkers > 0) {
        score -= (ktMetrics.todoErrorUnsupportedMarkers * 12).coerceAtMost(45)
        findings += "Output contains ${ktMetrics.todoErrorUnsupportedMarkers} TODO/ERROR/UnsupportedOperationException markers."
    }
    if (source.anonymousClasses > 0 && ktMetrics.anonymousConversionSignals == 0) {
        score -= 8
        findings += "Anonymous classes were present but no Kotlin lambda/object-expression signal was detected."
    }
    if (ktMetrics.anonymousClasses > 0) {
        score -= (ktMetrics.anonymousClasses * 6).coerceAtMost(18)
        findings += "Converted Kotlin still has ${ktMetrics.anonymousClasses} Java anonymous-class shaped markers."
    }
    if (source.annotations > 0 && ktMetrics.annotations < source.annotations / 2) {
        score -= 8
        findings += "Annotation count dropped sharply, which may break framework behavior."
    }

    val boundedScore = score.coerceIn(0, 100)
    val status = when {
        boundedScore >= 85 -> "good"
        boundedScore >= 65 -> "needs-review"
        else -> "poor"
    }
    return PairResult(relativePath, source, ktMetrics, status, boundedScore, 100 - boundedScore, findings.ifEmpty { listOf("No heuristic issues detected.") })
}

fun metrics(path: String, text: String): SourceFileMetrics {
    val noComments = text
        .replace(Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL), "")
        .replace(Regex("//.*"), "")
    val javaOnlySyntax = countMarkers(
        noComments,
        mapOf(
            "semicolon" to ";",
            "new_keyword" to "\\bnew\\s+",
            "public_modifier" to "\\bpublic\\s+",
            "private_modifier" to "\\bprivate\\s+",
            "protected_modifier" to "\\bprotected\\s+",
            "static_modifier" to "\\bstatic\\s+",
            "void_keyword" to "\\bvoid\\s+",
            "throws_keyword" to "\\bthrows\\s+",
        ),
    )
    val todoErrorUnsupported = countMarkers(
        text,
        mapOf(
            "TODO_call" to "\\bTODO\\s*\\(",
            "TODO_text" to "\\bTODO\\b",
            "ERROR_text" to "\\bERROR\\b",
            "UnsupportedOperationException" to "\\bUnsupportedOperationException\\b",
            "converter_error_marker" to "<caret>|/\\*__",
        ),
    )
    return SourceFileMetrics(
        path = path,
        lines = text.lines().count { it.isNotBlank() },
        classes = Regex("\\bclass\\s+[A-Za-z_][A-Za-z0-9_]*").findAll(noComments).count(),
        interfaces = Regex("\\binterface\\s+[A-Za-z_][A-Za-z0-9_]*").findAll(noComments).count(),
        enums = Regex("\\benum\\s+(class\\s+)?[A-Za-z_][A-Za-z0-9_]*").findAll(noComments).count(),
        records = Regex("\\brecord\\s+[A-Za-z_][A-Za-z0-9_]*").findAll(noComments).count(),
        methods = Regex("\\b[A-Za-z_][A-Za-z0-9_<>, ?\\[\\]]+\\s+[A-Za-z_][A-Za-z0-9_]*\\s*\\(").findAll(noComments).count(),
        functions = Regex("\\bfun\\s+[A-Za-z_][A-Za-z0-9_]*\\s*\\(").findAll(noComments).count(),
        annotations = Regex("@[A-Za-z_][A-Za-z0-9_.]*").findAll(noComments).count(),
        lambdas = Regex("(->|=>|\\{\\s*[A-Za-z_, ]+\\s*->)").findAll(noComments).count(),
        objectExpressions = Regex("\\bobject\\s*:").findAll(noComments).count(),
        anonymousClasses = Regex("new\\s+[A-Za-z_][A-Za-z0-9_<>.]*\\s*\\([^)]*\\)\\s*\\{").findAll(noComments).count(),
        javaOnlySyntaxMarkers = javaOnlySyntax.values.sum(),
        javaOnlySyntaxDetails = javaOnlySyntax,
        nullabilitySignals = Regex("[!?]|\\bOptional\\b|\\bObjects\\.requireNonNull\\b").findAll(noComments).count(),
        todoErrorUnsupportedMarkers = todoErrorUnsupported.values.sum(),
        todoErrorUnsupportedDetails = todoErrorUnsupported,
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
    val aggregate = aggregateMetrics(results, convertedFiles)
    val statuses = results.groupingBy { it.status }.eachCount().toSortedMap()
    val risky = mostSuspicious(results, 10)
    val sourceHash = stableDirectoryHash(sourceRoot)
    val convertedHash = stableDirectoryHash(convertedRoot)

    return buildString {
        appendLine("# Static J2K Evaluation Summary")
        appendLine()
        appendLine("- Source project: `$sourceLabel`")
        appendLine("- Converted project: `$convertedLabel`")
        appendLine("- Java files considered: ${aggregate.sourceFiles}")
        appendLine("- Kotlin files produced: ${aggregate.convertedFiles}")
        appendLine("- Matched Java/Kotlin pairs: ${aggregate.matchedFiles}")
        appendLine("- File coverage: ${aggregate.fileCoveragePercent}%")
        appendLine("- Average quality score: ${aggregate.averageScore}/100")
        appendLine("- Average suspiciousness: ${aggregate.averageSuspiciousness}/100")
        appendLine("- Status counts: ${statuses.entries.joinToString { "${it.key}=${it.value}" }}")
        appendLine("- Source hash: `$sourceHash`")
        appendLine("- Converted hash: `$convertedHash`")
        appendLine()
        appendLine("## Preservation Metrics")
        appendLine()
        appendLine("| Metric | Java source | Kotlin output |")
        appendLine("|---|---:|---:|")
        appendLine("| Classes | ${aggregate.sourceClasses} | ${aggregate.convertedClasses} |")
        appendLine("| Interfaces | ${aggregate.sourceInterfaces} | ${aggregate.convertedInterfaces} |")
        appendLine("| Enums | ${aggregate.sourceEnums} | ${aggregate.convertedEnums} |")
        appendLine("| Methods/functions | ${aggregate.sourceMethods} | ${aggregate.convertedMethods} |")
        appendLine("| Annotations | ${aggregate.sourceAnnotations} | ${aggregate.convertedAnnotations} |")
        appendLine("| Anonymous classes/signals | ${aggregate.sourceAnonymousClasses} | ${aggregate.convertedAnonymousSignals} |")
        appendLine()
        appendLine("## Marker Metrics")
        appendLine()
        appendLine("- Java-only syntax markers remaining in Kotlin: ${aggregate.javaOnlySyntaxMarkers}")
        appendLine("- TODO/ERROR/UnsupportedOperationException markers: ${aggregate.todoErrorUnsupportedMarkers}")
        appendLine()
        appendLine("## Top 10 Most Suspicious Files")
        appendLine()
        appendLine("| Suspiciousness | Score | Status | File | Primary finding |")
        appendLine("|---:|---:|---|---|---|")
        risky.forEach {
            appendLine("| ${it.suspiciousness} | ${it.score} | ${it.status} | `${it.relativePath}` | ${it.findings.first().escapeTable()} |")
        }
        appendLine()
        appendLine("## Interpretation")
        appendLine()
        appendLine("The evaluator is intentionally structural: it does not claim semantic equivalence. It checks whether conversion preserved file coverage, declaration shape, annotations, method-like declarations, and whether generated Kotlin still contains Java-only syntax or unresolved markers. Files marked `poor` should be manually inspected first.")
    }
}

fun buildCsv(results: List<PairResult>): String = buildString {
    appendLine("file,status,score,suspiciousness,java_lines,kotlin_lines,java_classes,kotlin_classes,java_interfaces,kotlin_interfaces,java_enums,kotlin_enums,java_methods,kotlin_functions,java_annotations,kotlin_annotations,kotlin_java_only_markers,kotlin_todo_error_unsupported_markers,java_anonymous_classes,kotlin_anonymous_signals,findings")
    results.forEach {
        appendLine(listOf(
            it.relativePath,
            it.status,
            it.score.toString(),
            it.suspiciousness.toString(),
            it.source.lines.toString(),
            it.converted?.lines?.toString() ?: "",
            it.source.classes.toString(),
            it.converted?.classes?.toString() ?: "",
            it.source.interfaces.toString(),
            it.converted?.interfaces?.toString() ?: "",
            it.source.enums.toString(),
            it.converted?.enums?.toString() ?: "",
            it.source.callableDeclarations.toString(),
            it.converted?.callableDeclarations?.toString() ?: "",
            it.source.annotations.toString(),
            it.converted?.annotations?.toString() ?: "",
            it.converted?.javaOnlySyntaxMarkers?.toString() ?: "",
            it.converted?.todoErrorUnsupportedMarkers?.toString() ?: "",
            it.source.anonymousClasses.toString(),
            it.converted?.anonymousConversionSignals?.toString() ?: "",
            it.findings.joinToString("; "),
        ).joinToString(",") { cell -> csv(cell) })
    }
}

fun buildJson(results: List<PairResult>): String = buildString {
    val aggregate = aggregateMetrics(results, emptyList())
    val top = mostSuspicious(results, 10)
    appendLine("{")
    appendLine("  \"summary\": {")
    appendLine("    \"sourceFiles\": ${aggregate.sourceFiles},")
    appendLine("    \"matchedFiles\": ${aggregate.matchedFiles},")
    appendLine("    \"fileCoveragePercent\": ${aggregate.fileCoveragePercent},")
    appendLine("    \"averageScore\": ${aggregate.averageScore},")
    appendLine("    \"averageSuspiciousness\": ${aggregate.averageSuspiciousness},")
    appendLine("    \"javaOnlySyntaxMarkers\": ${aggregate.javaOnlySyntaxMarkers},")
    appendLine("    \"todoErrorUnsupportedMarkers\": ${aggregate.todoErrorUnsupportedMarkers},")
    appendLine("    \"sourceAnonymousClasses\": ${aggregate.sourceAnonymousClasses},")
    appendLine("    \"convertedAnonymousSignals\": ${aggregate.convertedAnonymousSignals}")
    appendLine("  },")
    appendLine("  \"topSuspiciousFiles\": [")
    top.forEachIndexed { index, result ->
        append("    ${fileJson(result)}")
        appendLine(if (index == top.lastIndex) "" else ",")
    }
    appendLine("  ],")
    appendLine("  \"files\": [")
    results.forEachIndexed { index, result ->
        append("    ${fileJson(result)}")
        appendLine(if (index == results.lastIndex) "" else ",")
    }
    appendLine("  ]")
    appendLine("}")
}

fun fileJson(result: PairResult): String = buildString {
    append("{")
    append("\"file\":\"${json(result.relativePath)}\",")
    append("\"status\":\"${result.status}\",")
    append("\"score\":${result.score},")
    append("\"suspiciousness\":${result.suspiciousness},")
    append("\"source\":${metricsJson(result.source)},")
    append("\"converted\":${result.converted?.let { metricsJson(it) } ?: "null"},")
    append("\"findings\":[${result.findings.joinToString(",") { "\"${json(it)}\"" }}]")
    append("}")
}

fun metricsJson(metrics: SourceFileMetrics): String = buildString {
    append("{")
    append("\"lines\":${metrics.lines},")
    append("\"classes\":${metrics.classes},")
    append("\"interfaces\":${metrics.interfaces},")
    append("\"enums\":${metrics.enums},")
    append("\"records\":${metrics.records},")
    append("\"methods\":${metrics.methods},")
    append("\"functions\":${metrics.functions},")
    append("\"annotations\":${metrics.annotations},")
    append("\"lambdas\":${metrics.lambdas},")
    append("\"objectExpressions\":${metrics.objectExpressions},")
    append("\"anonymousClasses\":${metrics.anonymousClasses},")
    append("\"anonymousConversionSignals\":${metrics.anonymousConversionSignals},")
    append("\"javaOnlySyntaxMarkers\":${metrics.javaOnlySyntaxMarkers},")
    append("\"javaOnlySyntaxDetails\":${mapJson(metrics.javaOnlySyntaxDetails)},")
    append("\"todoErrorUnsupportedMarkers\":${metrics.todoErrorUnsupportedMarkers},")
    append("\"todoErrorUnsupportedDetails\":${mapJson(metrics.todoErrorUnsupportedDetails)}")
    append("}")
}

fun aggregateMetrics(results: List<PairResult>, convertedFiles: List<File>): AggregateMetrics {
    val sourceFiles = results.size
    val matchedFiles = results.count { it.converted != null }
    val convertedFileCount = if (convertedFiles.isEmpty()) matchedFiles else convertedFiles.size
    val averageScore = if (results.isEmpty()) 0 else results.map { it.score }.average().roundToInt()
    val averageSuspiciousness = if (results.isEmpty()) 0 else results.map { it.suspiciousness }.average().roundToInt()
    return AggregateMetrics(
        sourceFiles = sourceFiles,
        convertedFiles = convertedFileCount,
        matchedFiles = matchedFiles,
        fileCoveragePercent = ratioPercent(matchedFiles, sourceFiles),
        averageScore = averageScore,
        averageSuspiciousness = averageSuspiciousness,
        sourceClasses = results.sumOf { it.source.classes },
        convertedClasses = results.sumOf { it.converted?.classes ?: 0 },
        sourceInterfaces = results.sumOf { it.source.interfaces },
        convertedInterfaces = results.sumOf { it.converted?.interfaces ?: 0 },
        sourceEnums = results.sumOf { it.source.enums },
        convertedEnums = results.sumOf { it.converted?.enums ?: 0 },
        sourceMethods = results.sumOf { it.source.callableDeclarations },
        convertedMethods = results.sumOf { it.converted?.callableDeclarations ?: 0 },
        sourceAnnotations = results.sumOf { it.source.annotations },
        convertedAnnotations = results.sumOf { it.converted?.annotations ?: 0 },
        javaOnlySyntaxMarkers = results.sumOf { it.converted?.javaOnlySyntaxMarkers ?: 0 },
        todoErrorUnsupportedMarkers = results.sumOf { it.converted?.todoErrorUnsupportedMarkers ?: 0 },
        sourceAnonymousClasses = results.sumOf { it.source.anonymousClasses },
        convertedAnonymousSignals = results.sumOf { it.converted?.anonymousConversionSignals ?: 0 },
    )
}

fun mostSuspicious(results: List<PairResult>, limit: Int): List<PairResult> =
    results.sortedWith(compareByDescending<PairResult> { it.suspiciousness }.thenBy { it.relativePath }).take(limit)

fun countMarkers(text: String, patterns: Map<String, String>): Map<String, Int> =
    patterns.mapValues { (_, pattern) -> Regex(pattern).findAll(text).count() }

val SourceFileMetrics.classLikeDeclarations: Int
    get() = classes + interfaces + enums + records

val SourceFileMetrics.callableDeclarations: Int
    get() = methods + functions

val SourceFileMetrics.anonymousConversionSignals: Int
    get() = lambdas + objectExpressions

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
fun ratioPercent(numerator: Int, denominator: Int): Int = if (denominator == 0) 0 else ((numerator.toDouble() / denominator) * 100).roundToInt()
fun String.escapeTable(): String = replace("|", "\\|")
fun csv(value: String): String = "\"" + value.replace("\"", "\"\"") + "\""
fun json(value: String): String = value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
fun mapJson(values: Map<String, Int>): String = values.entries.joinToString(prefix = "{", postfix = "}") {
    "\"${json(it.key)}\":${it.value}"
}
