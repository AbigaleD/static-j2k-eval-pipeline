# Kotlin Compile Smoke Test

- Status: `failed`
- Kotlin files considered: 412
- Errors: 1
- Warnings: 0
- Message: Generated Kotlin did not compile in a standalone smoke test. This is recorded as a diagnostic signal only; it does not fail the pipeline.

## First Diagnostics

```text
exception: kotlin.UninitializedPropertyAccessException: lateinit property firType has not been initialized
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertNullableType(LightTreeRawFirDeclarationBuilder.kt:2313)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertNullableType$default(LightTreeRawFirDeclarationBuilder.kt:2292)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertNullableType(LightTreeRawFirDeclarationBuilder.kt:2304)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertNullableType$default(LightTreeRawFirDeclarationBuilder.kt:2292)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertType(LightTreeRawFirDeclarationBuilder.kt:2224)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertTypeProjection(LightTreeRawFirDeclarationBuilder.kt:2396)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertTypeArguments(LightTreeRawFirDeclarationBuilder.kt:2381)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertUserType(LightTreeRawFirDeclarationBuilder.kt:2338)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertUserType$default(LightTreeRawFirDeclarationBuilder.kt:2319)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertType(LightTreeRawFirDeclarationBuilder.kt:2223)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertTypeProjection(LightTreeRawFirDeclarationBuilder.kt:2396)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertTypeArguments(LightTreeRawFirDeclarationBuilder.kt:2381)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertUserType(LightTreeRawFirDeclarationBuilder.kt:2338)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertUserType$default(LightTreeRawFirDeclarationBuilder.kt:2319)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertType(LightTreeRawFirDeclarationBuilder.kt:2223)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertTypeProjection(LightTreeRawFirDeclarationBuilder.kt:2396)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertTypeArguments(LightTreeRawFirDeclarationBuilder.kt:2381)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertUserType(LightTreeRawFirDeclarationBuilder.kt:2338)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertNullableType(LightTreeRawFirDeclarationBuilder.kt:2302)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertNullableType$default(LightTreeRawFirDeclarationBuilder.kt:2292)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertType(LightTreeRawFirDeclarationBuilder.kt:2224)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertPropertyDeclaration(LightTreeRawFirDeclarationBuilder.kt:1305)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertClassBody(LightTreeRawFirDeclarationBuilder.kt:877)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertClass(LightTreeRawFirDeclarationBuilder.kt:587)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertBlockExpressionWithoutBuilding(LightTreeRawFirDeclarationBuilder.kt:138)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertBlockExpressionWithoutBuilding$default(LightTreeRawFirDeclarationBuilder.kt:134)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertBlockExpression(LightTreeRawFirDeclarationBuilder.kt:131)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertBlock(LightTreeRawFirDeclarationBuilder.kt:1987)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertFunctionBody(LightTreeRawFirDeclarationBuilder.kt:1955)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertFunctionDeclaration(LightTreeRawFirDeclarationBuilder.kt:1913)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertClassBody(LightTreeRawFirDeclarationBuilder.kt:876)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertClass(LightTreeRawFirDeclarationBuilder.kt:587)
	at org.jetbrains.kotlin.fir.lightTree.converter.LightTreeRawFirDeclarationBuilder.convertFile(LightTreeRawFirDeclarationBuilder.kt:95)
	at org.jetbrains.kotlin.fir.lightTree.LightTree2Fir.buildFirFile(LightTree2Fir.kt:91)
	at org.jetbrains.kotlin.fir.lightTree.LightTree2Fir.buildFirFile(LightTree2Fir.kt:97)
	at org.jetbrains.kotlin.fir.pipeline.FirUtilsKt.buildFirViaLightTree(firUtils.kt:40)
	at org.jetbrains.kotlin.fir.pipeline.FirUtilsKt.buildResolveAndCheckFirViaLightTree(firUtils.kt:87)
	at org.jetbrains.kotlin.cli.jvm.compiler.pipeline.JvmCompilerPipelineKt.compileModuleToAnalyzedFir(jvmCompilerPipeline.kt:319)
	at org.jetbrains.kotlin.cli.jvm.compiler.pipeline.JvmCompilerPipelineKt.compileModulesUsingFrontendIrAndLightTree(jvmCompilerPipeline.kt:118)
	at org.jetbrains.kotlin.cli.jvm.K2JVMCompiler.doExecute(K2JVMCompiler.kt:148)
	at org.jetbrains.kotlin.cli.jvm.K2JVMCompiler.doExecute(K2JVMCompiler.kt:43)
	at org.jetbrains.kotlin.cli.common.CLICompiler.execImpl(CLICompiler.kt:103)
	at org.jetbrains.kotlin.cli.common.CLICompiler.execImpl(CLICompiler.kt:49)
	at org.jetbrains.kotlin.cli.common.CLITool.exec(CLITool.kt:101)
	at org.jetbrains.kotlin.cli.common.CLITool.exec(CLITool.kt:79)
	at org.jetbrains.kotlin.cli.common.CLITool.exec(CLITool.kt:43)
	at org.jetbrains.kotlin.cli.common.CLITool$Companion.doMainNoExit(CLITool.kt:180)
	at org.jetbrains.kotlin.cli.common.CLITool$Companion.doMainNoExit$default(CLITool.kt:173)
	at org.jetbrains.kotlin.cli.common.CLITool$Companion.doMain(CLITool.kt:167)
	at org.jetbrains.kotlin.cli.jvm.K2JVMCompiler$Companion.main(K2JVMCompiler.kt:255)
	at org.jetbrains.kotlin.cli.jvm.K2JVMCompiler.main(K2JVMCompiler.kt)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:64)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:564)
	at org.jetbrains.kotlin.preloading.Preloader.run(Preloader.java:87)
	at org.jetbrains.kotlin.preloading.Preloader.main(Preloader.java:44)

```
