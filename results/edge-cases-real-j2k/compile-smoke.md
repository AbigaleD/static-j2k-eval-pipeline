# Kotlin Compile Smoke Test

- Status: `failed`
- Kotlin files considered: 8
- Errors: 48
- Warnings: 0
- Message: Generated Kotlin did not compile in a standalone smoke test. This is recorded as a diagnostic signal only; it does not fail the pipeline.

## First Diagnostics

```text
converted/edge-cases-real-j2k/cases/AnnotationDenseCommand.kt:12:1: error: only members in named objects and companion objects can be annotated with '@JvmStatic'.
@JvmStatic  fun run() {
^^^^^^^^^^
converted/edge-cases-real-j2k/cases/AnnotationDenseCommand.kt:12:17: error: 'run' hides member of supertype 'Runnable' and needs an 'override' modifier.
@JvmStatic  fun run() {
                ^^^
converted/edge-cases-real-j2k/cases/BuilderPatternWithFluentApi.kt:10:6: error: 'val' cannot be reassigned.
this.host = builder.host
     ^^^^
converted/edge-cases-real-j2k/cases/BuilderPatternWithFluentApi.kt:10:21: error: cannot access 'val host: String?': it is private in 'cases/BuilderPatternWithFluentApi.Builder'.
this.host = builder.host
                    ^^^^
converted/edge-cases-real-j2k/cases/BuilderPatternWithFluentApi.kt:11:6: error: 'val' cannot be reassigned.
this.port = builder.port
     ^^^^
converted/edge-cases-real-j2k/cases/BuilderPatternWithFluentApi.kt:11:21: error: cannot access 'val port: Int': it is private in 'cases/BuilderPatternWithFluentApi.Builder'.
this.port = builder.port
                    ^^^^
converted/edge-cases-real-j2k/cases/BuilderPatternWithFluentApi.kt:12:6: error: 'val' cannot be reassigned.
this.tlsEnabled = builder.tlsEnabled
     ^^^^^^^^^^
converted/edge-cases-real-j2k/cases/BuilderPatternWithFluentApi.kt:12:27: error: cannot access 'val tlsEnabled: Boolean': it is private in 'cases/BuilderPatternWithFluentApi.Builder'.
this.tlsEnabled = builder.tlsEnabled
                          ^^^^^^^^^^
converted/edge-cases-real-j2k/cases/BuilderPatternWithFluentApi.kt:13:6: error: 'val' cannot be reassigned.
this.timeoutMillis = builder.timeoutMillis
     ^^^^^^^^^^^^^
converted/edge-cases-real-j2k/cases/BuilderPatternWithFluentApi.kt:13:30: error: cannot access 'val timeoutMillis: Int': it is private in 'cases/BuilderPatternWithFluentApi.Builder'.
this.timeoutMillis = builder.timeoutMillis
                             ^^^^^^^^^^^^^
converted/edge-cases-real-j2k/cases/BuilderPatternWithFluentApi.kt:39:6: error: 'val' cannot be reassigned.
this.host = host
     ^^^^
converted/edge-cases-real-j2k/cases/BuilderPatternWithFluentApi.kt:43:6: error: 'val' cannot be reassigned.
this.port = port
     ^^^^
converted/edge-cases-real-j2k/cases/BuilderPatternWithFluentApi.kt:48:6: error: 'val' cannot be reassigned.
this.tlsEnabled = tlsEnabled
     ^^^^^^^^^^
converted/edge-cases-real-j2k/cases/BuilderPatternWithFluentApi.kt:53:6: error: 'val' cannot be reassigned.
this.timeoutMillis = timeoutMillis
     ^^^^^^^^^^^^^
converted/edge-cases-real-j2k/cases/BuilderPatternWithFluentApi.kt:77:8: error: cannot access 'constructor(host: String): BuilderPatternWithFluentApi.Builder': it is private in 'cases/BuilderPatternWithFluentApi.Builder'.
return Builder(host)
       ^^^^^^^
converted/edge-cases-real-j2k/cases/DefaultMethodsAndNestedInterfaces.kt:31:6: error: 'val' cannot be reassigned.
this.prefix = prefix
     ^^^^^^
converted/edge-cases-real-j2k/cases/DefaultMethodsAndNestedInterfaces.kt:35:28: error: return type of 'name' is not a subtype of the return type of the overridden member 'fun name(): String' defined in 'cases/DefaultMethodsAndNestedInterfaces'.
public override fun name():String? {
                           ^^^^^^^
converted/edge-cases-real-j2k/cases/GenericWildcardsAndCheckedExceptions.kt:9:14: error: cannot infer type for this parameter. Please specify it explicitly.
val result = ArrayList()
             ^^^^^^^^^
converted/edge-cases-real-j2k/cases/GenericWildcardsAndCheckedExceptions.kt:9:14: error: not enough information to infer type argument for 'E'.
val result = ArrayList()
             ^^^^^^^^^
converted/edge-cases-real-j2k/cases/GenericWildcardsAndCheckedExceptions.kt:12:10: error: unresolved reference 'doubleValue'.
if (item.doubleValue() < 0)
         ^^^^^^^^^^^
converted/edge-cases-real-j2k/cases/NestedAnonymousClasses.kt:7:33: error: this type does not have a constructor.
return object:Comparator<String>() {
                                ^^
converted/edge-cases-real-j2k/cases/NestedAnonymousClasses.kt:9:6: error: 'compare' hides member of supertype 'Comparator' and needs an 'override' modifier.
 fun compare(left:String, right:String):Int {
     ^^^^^^^
converted/edge-cases-real-j2k/cases/NestedAnonymousClasses.kt:10:39: error: this type does not have a constructor.
val nested = object:Comparator<String>() {
                                      ^^
converted/edge-cases-real-j2k/cases/NestedAnonymousClasses.kt:12:6: error: 'compare' hides member of supertype 'Comparator' and needs an 'override' modifier.
 fun compare(a:String, b:String):Int {
     ^^^^^^^
converted/edge-cases-real-j2k/cases/NestedAnonymousClasses.kt:13:10: error: unresolved reference 'compareToIgnoreCase'.
return a.compareToIgnoreCase(b)
         ^^^^^^^^^^^^^^^^^^^
converted/edge-cases-real-j2k/cases/NestedAnonymousClasses.kt:16:35: error: expression 'length' of type 'kotlin.Int' cannot be invoked as a function. Function 'invoke()' is not found.
val length = Integer.compare(left.length(), right.length())
                                  ^^^^^^
converted/edge-cases-real-j2k/cases/NestedAnonymousClasses.kt:16:51: error: expression 'length' of type 'kotlin.Int' cannot be invoked as a function. Function 'invoke()' is not found.
val length = Integer.compare(left.length(), right.length())
```
