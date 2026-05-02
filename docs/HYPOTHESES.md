# Conversion Hypotheses

These hypotheses guide the real-world benchmark and the custom edge-case dataset.

| Area | Hypothesis | Failure Signal |
|---|---|---|
| Wildcard generics | Java wildcard bounds such as `List<? extends T>` and multi-bound type parameters may convert to imprecise Kotlin variance or overly broad types. | Lost `out`/`in` variance, bounds collapsed to `Any`, or nullable types introduced without evidence. |
| Nested anonymous classes | Nested anonymous classes implementing SAM interfaces may remain verbose or malformed instead of becoming lambdas or valid `object` expressions. | Remaining `new Interface()`, invalid override syntax, or no lambda/object-expression signal. |
| Annotations and use-site targets | Runtime annotations, array-valued annotation arguments, and annotation targets may be dropped or mapped to the wrong Kotlin use-site target. | Missing annotations, missing array arguments, or incorrect `@field:`, `@get:`, `@param:` placement. |
| Static members | Static fields, constants, factory methods, and nested classes may not map cleanly to companion objects, top-level declarations, or `const val`. | Remaining `static`, broken Java callers, missing `@JvmStatic`, or non-constant values emitted as `const val`. |
| Overloaded methods | Java overload sets and varargs may convert into Kotlin signatures that compile but change source or binary interop. | Ambiguous overloads, missing `vararg`, missing spread operator usage, or Java call sites no longer resolving. |
| Checked exceptions | Java checked exceptions may lose API documentation or Java interop metadata during conversion. | Remaining raw `throws`, missing `@Throws`, or changed behavior around exception propagation. |
| Static initializers and derived constants | Static initializer blocks may be difficult to express as Kotlin constants while preserving initialization order and Java access. | Derived values move to the wrong initializer, `const val` is used incorrectly, or Java callers see a different API. |
| Raw legacy collections | Raw `List` and `Map` usage forces the converter to choose between unsafe Kotlin casts and broad `MutableList<Any?>` style types. | Hidden unchecked casts, excessive `as` casts, or loss of useful type information. |
| Default methods and nested interfaces | Java interface defaults and nested interfaces require careful mapping to Kotlin interface bodies and nested declarations. | Default implementations disappear, nested references break, or anonymous implementations become malformed. |
| Fluent builder pattern | Classic builders combine mutable construction, private final output fields, validation, and chainable setters. | Fluent setters stop returning `Builder`, validation order changes, or immutable fields become mutable public state. |

The evaluator currently treats these as review targets. A production evaluator should validate them with PSI-based matching, compilation, and selected behavioral tests.
