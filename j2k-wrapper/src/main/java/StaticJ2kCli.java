import com.intellij.core.CoreApplicationEnvironment;
import com.intellij.core.JavaCoreApplicationEnvironment;
import com.intellij.core.JavaCoreProjectEnvironment;
import com.intellij.lang.MetaLanguage;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.lang.java.syntax.JavaElementTypeConverterExtension;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.extensions.ExtensionPoint;
import com.intellij.openapi.extensions.ExtensionsArea;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.mock.MockComponentManager;
import com.intellij.codeInsight.ContextNullabilityInfo;
import com.intellij.codeInsight.InferredAnnotationsManager;
import com.intellij.codeInsight.NullableNotNullManager;
import com.intellij.codeInsight.Nullability;
import com.intellij.codeInsight.NullabilityAnnotationInfo;
import com.intellij.codeInsight.ExternalAnnotationsManager;
import com.intellij.codeInsight.annoPackages.AnnotationPackageSupport;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.PsiElementFinder;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.JavaClassSupers;
import com.intellij.psi.augment.PsiAugmentProvider;
import com.intellij.util.QueryExecutor;
import com.intellij.psi.TokenType;
import com.intellij.platform.syntax.element.SyntaxTokenTypes;
import com.intellij.platform.syntax.psi.ElementTypeConverterFactory;
import com.intellij.platform.syntax.psi.ElementTypeConverterKt;
import com.intellij.platform.syntax.psi.ElementTypeConverters;
import kotlin.TuplesKt;
import org.jetbrains.kotlin.j2k.ConverterSettings;
import org.jetbrains.kotlin.j2k.ElementResult;
import org.jetbrains.kotlin.j2k.OldJavaToKotlinConverter;
import org.jetbrains.kotlin.j2k.Result;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.lang.reflect.Proxy;

public final class StaticJ2kCli {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: StaticJ2kCli <input.java> <output.kt>");
            System.exit(64);
        }

        Path input = Path.of(args[0]);
        Path output = Path.of(args[1]);
        String javaText = Files.readString(input, StandardCharsets.UTF_8);

        Disposable disposable = Disposer.newDisposable("static-j2k-cli");
        try {
            JavaCoreApplicationEnvironment appEnv = new JavaCoreApplicationEnvironment(disposable);
            ((MockComponentManager) appEnv.getApplication()).registerService(
                JavaClassSupers.class,
                new NoopJavaClassSupers()
            );
            CoreApplicationEnvironment.registerApplicationExtensionPoint(MetaLanguage.EP_NAME, MetaLanguage.class);
            CoreApplicationEnvironment.registerApplicationExtensionPoint(
                AnnotationPackageSupport.EP_NAME,
                AnnotationPackageSupport.class
            );
            CoreApplicationEnvironment.registerApplicationExtensionPoint(
                PsiAugmentProvider.EP_NAME,
                PsiAugmentProvider.class
            );
            registerEmptyQueryExtensionPoints(appEnv.getApplication().getExtensionArea());
            registerBundledExtensions(appEnv);
            appEnv.addExplicitExtension(
                ElementTypeConverters.getInstance(),
                JavaLanguage.INSTANCE,
                (ElementTypeConverterFactory) () -> ElementTypeConverterKt.elementTypeConverterOf(
                    TuplesKt.to(SyntaxTokenTypes.getWHITE_SPACE(), TokenType.WHITE_SPACE),
                    TuplesKt.to(SyntaxTokenTypes.getBAD_CHARACTER(), TokenType.BAD_CHARACTER),
                    TuplesKt.to(SyntaxTokenTypes.getERROR_ELEMENT(), TokenType.ERROR_ELEMENT)
                )
            );
            appEnv.addExplicitExtension(
                ElementTypeConverters.getInstance(),
                JavaLanguage.INSTANCE,
                new JavaElementTypeConverterExtension()
            );
            JavaCoreProjectEnvironment projectEnv = new JavaCoreProjectEnvironment(disposable, appEnv);
            Project project = projectEnv.getProject();
            ((MockComponentManager) project).getExtensionArea().registerExtensionPoint(
                "com.intellij.java.elementFinder",
                PsiElementFinder.class.getName(),
                ExtensionPoint.Kind.INTERFACE,
                false
            );
            ((MockComponentManager) project).registerService(
                NullableNotNullManager.class,
                new NoopNullableNotNullManager(project)
            );
            ((MockComponentManager) project).registerService(
                ExternalAnnotationsManager.class,
                new NoopExternalAnnotationsManager()
            );
            ((MockComponentManager) project).registerService(
                InferredAnnotationsManager.class,
                new NoopInferredAnnotationsManager()
            );
            ((MockComponentManager) project).registerService(
                PsiSearchHelper.class,
                noopPsiSearchHelper()
            );
            ProjectFileIndex projectFileIndex = noopProjectFileIndex();
            ((MockComponentManager) project).registerService(ProjectFileIndex.class, projectFileIndex);
            ((MockComponentManager) project).registerService(
                ProjectRootManager.class,
                new NoopProjectRootManager(projectFileIndex)
            );

            PsiFile psiFile = PsiFileFactory.getInstance(project)
                .createFileFromText(input.getFileName().toString(), JavaLanguage.INSTANCE, javaText);
            if (!(psiFile instanceof PsiJavaFile)) {
                throw new IllegalStateException("Input was not parsed as a PsiJavaFile: " + input);
            }

            ConverterSettings settings = new ConverterSettings(
                true,  // forceNotNullTypes
                false, // specifyLocalVariableTypeByDefault
                false, // specifyFieldTypeByDefault
                false, // openByDefault
                false, // publicByDefault
                false  // basicMode
            );

            OldJavaToKotlinConverter converter = new OldJavaToKotlinConverter(project, settings);
            Result result = converter.elementsToKotlin(Collections.singletonList((PsiJavaFile) psiFile));
            if (result.getResults().isEmpty()) {
                throw new IllegalStateException("J2K produced no result for " + input);
            }

            ElementResult converted = result.getResults().get(0);
            Files.createDirectories(output.getParent());
            Files.writeString(output, converted.getText(), StandardCharsets.UTF_8);
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            System.exit(1);
        } finally {
            Disposer.dispose(disposable);
        }
        System.exit(0);
    }

    private static void registerEmptyQueryExtensionPoints(ExtensionsArea area) {
        for (String name : List.of(
            "com.intellij.directClassInheritorsSearch",
            "com.intellij.classInheritorsSearch",
            "com.intellij.allClassesSearch",
            "com.intellij.referencesSearch",
            "com.intellij.methodReferencesSearch",
            "com.intellij.overridingMethodsSearch",
            "com.intellij.allOverridingMethodsSearch",
            "com.intellij.deepestSuperMethodsSearch",
            "com.intellij.annotatedElementsSearch",
            "com.intellij.functionalExpressionSearch"
        )) {
            if (!area.hasExtensionPoint(name)) {
                area.registerExtensionPoint(name, QueryExecutor.class.getName(), ExtensionPoint.Kind.INTERFACE, false);
            }
        }
    }

    private static PsiSearchHelper noopPsiSearchHelper() {
        return (PsiSearchHelper) Proxy.newProxyInstance(
            StaticJ2kCli.class.getClassLoader(),
            new Class<?>[]{PsiSearchHelper.class},
            (proxy, method, args) -> {
                Class<?> returnType = method.getReturnType();
                if (returnType == boolean.class) return false;
                if (returnType == int.class) return 0;
                if (returnType == long.class) return 0L;
                if (returnType == void.class) return null;
                if (returnType == PsiSearchHelper.SearchCostResult.class) {
                    return PsiSearchHelper.SearchCostResult.ZERO_OCCURRENCES;
                }
                if (returnType == SearchScope.class || returnType == GlobalSearchScope.class) {
                    return GlobalSearchScope.EMPTY_SCOPE;
                }
                if (returnType.isArray()) {
                    return java.lang.reflect.Array.newInstance(returnType.getComponentType(), 0);
                }
                return null;
            }
        );
    }

    private static ProjectFileIndex noopProjectFileIndex() {
        return (ProjectFileIndex) Proxy.newProxyInstance(
            StaticJ2kCli.class.getClassLoader(),
            new Class<?>[]{ProjectFileIndex.class},
            (proxy, method, args) -> {
                Class<?> returnType = method.getReturnType();
                if (returnType == boolean.class) return false;
                if (returnType == int.class) return 0;
                if (returnType == long.class) return 0L;
                if (returnType == void.class) return null;
                if (returnType == String.class) return null;
                if (returnType.isArray()) {
                    return java.lang.reflect.Array.newInstance(returnType.getComponentType(), 0);
                }
                if (List.class.isAssignableFrom(returnType)) return Collections.emptyList();
                if (java.util.Set.class.isAssignableFrom(returnType)) return Collections.emptySet();
                if (java.util.Collection.class.isAssignableFrom(returnType)) return Collections.emptyList();
                return null;
            }
        );
    }

    private static final class NoopExternalAnnotationsManager extends ExternalAnnotationsManager {
        @Override public boolean hasAnnotationRootsForFile(com.intellij.openapi.vfs.VirtualFile file) { return false; }
        @Override public boolean isExternalAnnotation(PsiAnnotation annotation) { return false; }
        @Override public PsiAnnotation findExternalAnnotation(PsiModifierListOwner owner, String annotationFQN) { return null; }
        @Override public List<PsiAnnotation> findExternalAnnotations(PsiModifierListOwner owner, String annotationFQN) { return Collections.emptyList(); }
        @Override public boolean isExternalAnnotationWritable(PsiModifierListOwner owner, String annotationFQN) { return false; }
        @Override public PsiAnnotation[] findExternalAnnotations(PsiModifierListOwner owner) { return PsiAnnotation.EMPTY_ARRAY; }
        @Override public List<PsiAnnotation> findDefaultConstructorExternalAnnotations(PsiClass psiClass) { return Collections.emptyList(); }
        @Override public List<PsiAnnotation> findDefaultConstructorExternalAnnotations(PsiClass psiClass, String annotationFQN) { return Collections.emptyList(); }
        @Override public void annotateExternally(PsiModifierListOwner owner, String annotationFQN, PsiFile fromFile, PsiNameValuePair[] value) {}
        @Override public boolean deannotate(PsiModifierListOwner owner, String annotationFQN) { return false; }
        @Override public boolean editExternalAnnotation(PsiModifierListOwner owner, String annotationFQN, PsiNameValuePair[] value) { return false; }
        @Override public AnnotationPlace chooseAnnotationsPlaceNoUi(PsiElement element) { return null; }
        @Override public AnnotationPlace chooseAnnotationsPlace(PsiElement element) { return null; }
        @Override public List<PsiFile> findExternalAnnotationsFiles(PsiModifierListOwner owner) { return Collections.emptyList(); }
        @Override public boolean hasConfiguredAnnotationRoot(PsiModifierListOwner owner) { return false; }
    }

    private static final class NoopInferredAnnotationsManager extends InferredAnnotationsManager {
        @Override public PsiAnnotation findInferredAnnotation(PsiModifierListOwner owner, String annotationFQN) { return null; }
        @Override public PsiAnnotation[] findInferredAnnotations(PsiModifierListOwner owner) { return PsiAnnotation.EMPTY_ARRAY; }
        @Override public boolean isInferredAnnotation(PsiAnnotation annotation) { return false; }
    }

    private static final class NoopNullableNotNullManager extends NullableNotNullManager {
        private static final String DEFAULT_NULLABLE = "org.jetbrains.annotations.Nullable";
        private static final String DEFAULT_NOT_NULL = "org.jetbrains.annotations.NotNull";

        private NoopNullableNotNullManager(Project project) {
            super(project);
        }

        @Override public List<String> getDefaultNullables() { return List.of(DEFAULT_NULLABLE); }
        @Override public List<String> getDefaultNotNulls() { return List.of(DEFAULT_NOT_NULL); }
        @Override public Optional<Nullability> getAnnotationNullability(String annotation) { return Optional.empty(); }
        @Override public boolean isTypeUseAnnotationLocationRestricted(String annotation) { return false; }
        @Override public boolean canAnnotateLocals(String annotation) { return false; }
        @Override public void setNotNulls(String... annotations) {}
        @Override public void setNullables(String... annotations) {}
        @Override public String getDefaultNullable() { return DEFAULT_NULLABLE; }
        @Override public void setDefaultNullable(String annotation) {}
        @Override public String getDefaultNotNull() { return DEFAULT_NOT_NULL; }
        @Override public void setDefaultNotNull(String annotation) {}
        @Override public NullabilityAnnotationInfo findEffectiveNullabilityInfo(PsiModifierListOwner owner) { return null; }
        @Override protected ContextNullabilityInfo getNullityDefault(PsiModifierListOwner owner, PsiAnnotation.TargetType[] placeTargetTypes) {
            return ContextNullabilityInfo.EMPTY;
        }
        @Override protected NullabilityAnnotationDataHolder getAllNullabilityAnnotationsWithNickNames() {
            return NullabilityAnnotationDataHolder.fromMap(Map.of(
                DEFAULT_NULLABLE, Nullability.NULLABLE,
                DEFAULT_NOT_NULL, Nullability.NOT_NULL
            ));
        }
        @Override public List<String> getNullables() { return List.of(DEFAULT_NULLABLE); }
        @Override public List<String> getNotNulls() { return List.of(DEFAULT_NOT_NULL); }
        @Override public List<String> getInstrumentedNotNulls() { return Collections.emptyList(); }
        @Override public void setInstrumentedNotNulls(List<String> annotations) {}
    }

    private static final class NoopProjectRootManager extends ProjectRootManager {
        private final ProjectFileIndex projectFileIndex;

        private NoopProjectRootManager(ProjectFileIndex projectFileIndex) {
            this.projectFileIndex = projectFileIndex;
        }

        @Override public ProjectFileIndex getFileIndex() { return projectFileIndex; }
        @Override public OrderEnumerator orderEntries() { return null; }
        @Override public OrderEnumerator orderEntries(java.util.Collection<? extends com.intellij.openapi.module.Module> modules) { return null; }
        @Override public com.intellij.openapi.vfs.VirtualFile[] getContentRootsFromAllModules() { return com.intellij.openapi.vfs.VirtualFile.EMPTY_ARRAY; }
        @Override public List<String> getContentRootUrls() { return Collections.emptyList(); }
        @Override public com.intellij.openapi.vfs.VirtualFile[] getContentRoots() { return com.intellij.openapi.vfs.VirtualFile.EMPTY_ARRAY; }
        @Override public com.intellij.openapi.vfs.VirtualFile[] getContentSourceRoots() { return com.intellij.openapi.vfs.VirtualFile.EMPTY_ARRAY; }
        @Override public List<com.intellij.openapi.vfs.VirtualFile> getModuleSourceRoots(java.util.Set<? extends org.jetbrains.jps.model.module.JpsModuleSourceRootType<?>> rootTypes) { return Collections.emptyList(); }
        @Override public Sdk getProjectSdk() { return null; }
        @Override public String getProjectSdkName() { return null; }
        @Override public String getProjectSdkTypeName() { return null; }
        @Override public void setProjectSdk(Sdk sdk) {}
        @Override public void setProjectSdkName(String name, String type) {}
        @Override public ModuleRootManager getModuleRootManager(com.intellij.openapi.module.Module module) { return null; }
    }

    private static final class NoopJavaClassSupers extends JavaClassSupers {
        @Override public PsiSubstitutor getSuperClassSubstitutor(
            PsiClass superClass,
            PsiClass derivedClass,
            GlobalSearchScope resolveScope,
            PsiSubstitutor derivedSubstitutor
        ) {
            return derivedSubstitutor == null ? PsiSubstitutor.EMPTY : derivedSubstitutor;
        }

        @Override public void reportHierarchyInconsistency(PsiClass superClass, PsiClass derivedClass) {}
    }

    private static void registerBundledExtensions(JavaCoreApplicationEnvironment appEnv) {
        String ideaHome = System.getenv("J2K_IDEA_HOME");
        if (ideaHome == null || ideaHome.isBlank()) {
            return;
        }

        List<String[]> descriptors = List.of(
            new String[]{"plugins/java", "com.intellij.java"},
            new String[]{"plugins/Kotlin", "org.jetbrains.kotlin"}
        );

        for (String[] descriptor : descriptors) {
            Path jar = Path.of(ideaHome, descriptor[0]);
            if (Files.isRegularFile(jar)) {
                CoreApplicationEnvironment.registerExtensionPointAndExtensions(
                    jar,
                    descriptor[1],
                    appEnv.getApplication().getExtensionArea()
                );
            }
        }
    }
}
