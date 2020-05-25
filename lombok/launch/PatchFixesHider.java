package lombok.launch;

import java.io.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.jdt.internal.corext.refactoring.structure.*;
import lombok.eclipse.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import java.util.*;
import org.eclipse.jdt.internal.core.dom.rewrite.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.corext.refactoring.*;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.core.resources.*;
import java.lang.reflect.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.*;

final class PatchFixesHider
{
    public static final class Util
    {
        private static ClassLoader shadowLoader;
        
        public static Class<?> shadowLoadClass(final String name) {
            try {
                if (Util.shadowLoader == null) {
                    try {
                        Class.forName("lombok.core.LombokNode");
                        Util.shadowLoader = Util.class.getClassLoader();
                    }
                    catch (ClassNotFoundException e) {
                        Util.shadowLoader = Main.createShadowClassLoader();
                    }
                }
                return Class.forName(name, true, Util.shadowLoader);
            }
            catch (ClassNotFoundException e) {
                throw sneakyThrow(e);
            }
        }
        
        public static Method findMethod(final Class<?> type, final String name, final Class<?>... parameterTypes) {
            try {
                return type.getDeclaredMethod(name, parameterTypes);
            }
            catch (NoSuchMethodException e) {
                throw sneakyThrow(e);
            }
        }
        
        public static Object invokeMethod(final Method method, final Object... args) {
            try {
                return method.invoke(null, args);
            }
            catch (IllegalAccessException e) {
                throw sneakyThrow(e);
            }
            catch (InvocationTargetException e2) {
                throw sneakyThrow(e2.getCause());
            }
        }
        
        private static RuntimeException sneakyThrow(final Throwable t) {
            if (t == null) {
                throw new NullPointerException("t");
            }
            sneakyThrow0(t);
            return null;
        }
        
        private static <T extends Throwable> void sneakyThrow0(final Throwable t) throws T, Throwable {
            throw t;
        }
    }
    
    public static final class LombokDeps
    {
        public static final Method ADD_LOMBOK_NOTES;
        public static final Method POST_COMPILER_BYTES_STRING;
        public static final Method POST_COMPILER_OUTPUTSTREAM;
        public static final Method POST_COMPILER_BUFFEREDOUTPUTSTREAM_STRING_STRING;
        
        public static String addLombokNotesToEclipseAboutDialog(final String origReturnValue, final String key) {
            return (String)Util.invokeMethod(LombokDeps.ADD_LOMBOK_NOTES, origReturnValue, key);
        }
        
        public static byte[] runPostCompiler(final byte[] bytes, final String fileName) {
            return (byte[])Util.invokeMethod(LombokDeps.POST_COMPILER_BYTES_STRING, bytes, fileName);
        }
        
        public static OutputStream runPostCompiler(final OutputStream out) throws IOException {
            return (OutputStream)Util.invokeMethod(LombokDeps.POST_COMPILER_OUTPUTSTREAM, out);
        }
        
        public static BufferedOutputStream runPostCompiler(final BufferedOutputStream out, final String path, final String name) throws IOException {
            return (BufferedOutputStream)Util.invokeMethod(LombokDeps.POST_COMPILER_BUFFEREDOUTPUTSTREAM_STRING_STRING, out, path, name);
        }
        
        static {
            final Class<?> shadowed = Util.shadowLoadClass("lombok.eclipse.agent.PatchFixesShadowLoaded");
            ADD_LOMBOK_NOTES = Util.findMethod(shadowed, "addLombokNotesToEclipseAboutDialog", String.class, String.class);
            POST_COMPILER_BYTES_STRING = Util.findMethod(shadowed, "runPostCompiler", byte[].class, String.class);
            POST_COMPILER_OUTPUTSTREAM = Util.findMethod(shadowed, "runPostCompiler", OutputStream.class);
            POST_COMPILER_BUFFEREDOUTPUTSTREAM_STRING_STRING = Util.findMethod(shadowed, "runPostCompiler", BufferedOutputStream.class, String.class, String.class);
        }
    }
    
    public static final class Transform
    {
        private static final Method TRANSFORM;
        private static final Method TRANSFORM_SWAPPED;
        
        public static void transform(final Parser parser, final CompilationUnitDeclaration ast) throws IOException {
            Util.invokeMethod(Transform.TRANSFORM, parser, ast);
        }
        
        public static void transform_swapped(final CompilationUnitDeclaration ast, final Parser parser) throws IOException {
            Util.invokeMethod(Transform.TRANSFORM_SWAPPED, ast, parser);
        }
        
        static {
            final Class<?> shadowed = Util.shadowLoadClass("lombok.eclipse.TransformEclipseAST");
            TRANSFORM = Util.findMethod(shadowed, "transform", Parser.class, CompilationUnitDeclaration.class);
            TRANSFORM_SWAPPED = Util.findMethod(shadowed, "transform_swapped", CompilationUnitDeclaration.class, Parser.class);
        }
    }
    
    public static final class Delegate
    {
        private static final Method HANDLE_DELEGATE_FOR_TYPE;
        
        public static boolean handleDelegateForType(final Object classScope) {
            return (boolean)Util.invokeMethod(Delegate.HANDLE_DELEGATE_FOR_TYPE, classScope);
        }
        
        static {
            final Class<?> shadowed = Util.shadowLoadClass("lombok.eclipse.agent.PatchDelegatePortal");
            HANDLE_DELEGATE_FOR_TYPE = Util.findMethod(shadowed, "handleDelegateForType", Object.class);
        }
    }
    
    public static final class ValPortal
    {
        private static final Method COPY_INITIALIZATION_OF_FOR_EACH_ITERABLE;
        private static final Method COPY_INITIALIZATION_OF_LOCAL_DECLARATION;
        private static final Method ADD_FINAL_AND_VAL_ANNOTATION_TO_VARIABLE_DECLARATION_STATEMENT;
        private static final Method ADD_FINAL_AND_VAL_ANNOTATION_TO_SINGLE_VARIABLE_DECLARATION;
        
        public static void copyInitializationOfForEachIterable(final Object parser) {
            Util.invokeMethod(ValPortal.COPY_INITIALIZATION_OF_FOR_EACH_ITERABLE, parser);
        }
        
        public static void copyInitializationOfLocalDeclaration(final Object parser) {
            Util.invokeMethod(ValPortal.COPY_INITIALIZATION_OF_LOCAL_DECLARATION, parser);
        }
        
        public static void addFinalAndValAnnotationToVariableDeclarationStatement(final Object converter, final Object out, final Object in) {
            Util.invokeMethod(ValPortal.ADD_FINAL_AND_VAL_ANNOTATION_TO_VARIABLE_DECLARATION_STATEMENT, converter, out, in);
        }
        
        public static void addFinalAndValAnnotationToSingleVariableDeclaration(final Object converter, final Object out, final Object in) {
            Util.invokeMethod(ValPortal.ADD_FINAL_AND_VAL_ANNOTATION_TO_SINGLE_VARIABLE_DECLARATION, converter, out, in);
        }
        
        static {
            final Class<?> shadowed = Util.shadowLoadClass("lombok.eclipse.agent.PatchValEclipsePortal");
            COPY_INITIALIZATION_OF_FOR_EACH_ITERABLE = Util.findMethod(shadowed, "copyInitializationOfForEachIterable", Object.class);
            COPY_INITIALIZATION_OF_LOCAL_DECLARATION = Util.findMethod(shadowed, "copyInitializationOfLocalDeclaration", Object.class);
            ADD_FINAL_AND_VAL_ANNOTATION_TO_VARIABLE_DECLARATION_STATEMENT = Util.findMethod(shadowed, "addFinalAndValAnnotationToVariableDeclarationStatement", Object.class, Object.class, Object.class);
            ADD_FINAL_AND_VAL_ANNOTATION_TO_SINGLE_VARIABLE_DECLARATION = Util.findMethod(shadowed, "addFinalAndValAnnotationToSingleVariableDeclaration", Object.class, Object.class, Object.class);
        }
    }
    
    public static final class Val
    {
        private static final Method SKIP_RESOLVE_INITIALIZER_IF_ALREADY_CALLED;
        private static final Method SKIP_RESOLVE_INITIALIZER_IF_ALREADY_CALLED2;
        private static final Method HANDLE_VAL_FOR_LOCAL_DECLARATION;
        private static final Method HANDLE_VAL_FOR_FOR_EACH;
        
        public static TypeBinding skipResolveInitializerIfAlreadyCalled(final Expression expr, final BlockScope scope) {
            return (TypeBinding)Util.invokeMethod(Val.SKIP_RESOLVE_INITIALIZER_IF_ALREADY_CALLED, expr, scope);
        }
        
        public static TypeBinding skipResolveInitializerIfAlreadyCalled2(final Expression expr, final BlockScope scope, final LocalDeclaration decl) {
            return (TypeBinding)Util.invokeMethod(Val.SKIP_RESOLVE_INITIALIZER_IF_ALREADY_CALLED2, expr, scope, decl);
        }
        
        public static boolean handleValForLocalDeclaration(final LocalDeclaration local, final BlockScope scope) {
            return (boolean)Util.invokeMethod(Val.HANDLE_VAL_FOR_LOCAL_DECLARATION, local, scope);
        }
        
        public static boolean handleValForForEach(final ForeachStatement forEach, final BlockScope scope) {
            return (boolean)Util.invokeMethod(Val.HANDLE_VAL_FOR_FOR_EACH, forEach, scope);
        }
        
        static {
            final Class<?> shadowed = Util.shadowLoadClass("lombok.eclipse.agent.PatchVal");
            SKIP_RESOLVE_INITIALIZER_IF_ALREADY_CALLED = Util.findMethod(shadowed, "skipResolveInitializerIfAlreadyCalled", Expression.class, BlockScope.class);
            SKIP_RESOLVE_INITIALIZER_IF_ALREADY_CALLED2 = Util.findMethod(shadowed, "skipResolveInitializerIfAlreadyCalled2", Expression.class, BlockScope.class, LocalDeclaration.class);
            HANDLE_VAL_FOR_LOCAL_DECLARATION = Util.findMethod(shadowed, "handleValForLocalDeclaration", LocalDeclaration.class, BlockScope.class);
            HANDLE_VAL_FOR_FOR_EACH = Util.findMethod(shadowed, "handleValForForEach", ForeachStatement.class, BlockScope.class);
        }
    }
    
    public static final class ExtensionMethod
    {
        private static final Method RESOLVE_TYPE;
        private static final Method ERROR_NO_METHOD_FOR;
        private static final Method INVALID_METHOD;
        private static final Method INVALID_METHOD2;
        
        public static TypeBinding resolveType(final TypeBinding resolvedType, final MessageSend methodCall, final BlockScope scope) {
            return (TypeBinding)Util.invokeMethod(ExtensionMethod.RESOLVE_TYPE, resolvedType, methodCall, scope);
        }
        
        public static void errorNoMethodFor(final ProblemReporter problemReporter, final MessageSend messageSend, final TypeBinding recType, final TypeBinding[] params) {
            Util.invokeMethod(ExtensionMethod.ERROR_NO_METHOD_FOR, problemReporter, messageSend, recType, params);
        }
        
        public static void invalidMethod(final ProblemReporter problemReporter, final MessageSend messageSend, final MethodBinding method) {
            Util.invokeMethod(ExtensionMethod.INVALID_METHOD, problemReporter, messageSend, method);
        }
        
        public static void invalidMethod(final ProblemReporter problemReporter, final MessageSend messageSend, final MethodBinding method, final Scope scope) {
            Util.invokeMethod(ExtensionMethod.INVALID_METHOD2, problemReporter, messageSend, method, scope);
        }
        
        static {
            final Class<?> shadowed = Util.shadowLoadClass("lombok.eclipse.agent.PatchExtensionMethod");
            RESOLVE_TYPE = Util.findMethod(shadowed, "resolveType", TypeBinding.class, MessageSend.class, BlockScope.class);
            ERROR_NO_METHOD_FOR = Util.findMethod(shadowed, "errorNoMethodFor", ProblemReporter.class, MessageSend.class, TypeBinding.class, TypeBinding[].class);
            INVALID_METHOD = Util.findMethod(shadowed, "invalidMethod", ProblemReporter.class, MessageSend.class, MethodBinding.class);
            INVALID_METHOD2 = Util.findMethod(shadowed, "invalidMethod", ProblemReporter.class, MessageSend.class, MethodBinding.class, Scope.class);
        }
    }
    
    public static final class PatchFixes
    {
        public static final int ALREADY_PROCESSED_FLAG = 8388608;
        
        public static boolean isGenerated(final ASTNode node) {
            boolean result = false;
            try {
                result = (boolean)node.getClass().getField("$isGenerated").get(node);
                if (!result && node.getParent() != null && node.getParent() instanceof QualifiedName) {
                    result = isGenerated(node.getParent());
                }
            }
            catch (Exception ex) {}
            return result;
        }
        
        public static boolean isListRewriteOnGeneratedNode(final ListRewrite rewrite) {
            return isGenerated(rewrite.getParent());
        }
        
        public static boolean returnFalse(final Object object) {
            return false;
        }
        
        public static boolean returnTrue(final Object object) {
            return true;
        }
        
        public static List removeGeneratedNodes(final List list) {
            try {
                final List realNodes = new ArrayList(list.size());
                for (final Object node : list) {
                    if (!isGenerated((ASTNode)node)) {
                        realNodes.add(node);
                    }
                }
                return realNodes;
            }
            catch (Exception ex) {
                return list;
            }
        }
        
        public static String getRealMethodDeclarationSource(final String original, final Object processor, final MethodDeclaration declaration) throws Exception {
            if (!isGenerated((ASTNode)declaration)) {
                return original;
            }
            final List<Annotation> annotations = new ArrayList<Annotation>();
            for (final Object modifier : declaration.modifiers()) {
                if (modifier instanceof Annotation) {
                    final Annotation annotation = (Annotation)modifier;
                    final String qualifiedAnnotationName = annotation.resolveTypeBinding().getQualifiedName();
                    if ("java.lang.Override".equals(qualifiedAnnotationName) || "java.lang.SuppressWarnings".equals(qualifiedAnnotationName)) {
                        continue;
                    }
                    annotations.add(annotation);
                }
            }
            final StringBuilder signature = new StringBuilder();
            addAnnotations(annotations, signature);
            if (processor.getClass().getDeclaredField("fPublic").get(processor)) {
                signature.append("public ");
            }
            if (processor.getClass().getDeclaredField("fAbstract").get(processor)) {
                signature.append("abstract ");
            }
            signature.append(declaration.getReturnType2().toString()).append(" ").append(declaration.getName().getFullyQualifiedName()).append("(");
            boolean first = true;
            for (final Object parameter : declaration.parameters()) {
                if (!first) {
                    signature.append(", ");
                }
                first = false;
                signature.append(parameter);
            }
            signature.append(");");
            return signature.toString();
        }
        
        public static void addAnnotations(final List<Annotation> annotations, final StringBuilder signature) {
            for (final Annotation annotation : annotations) {
                final List<String> values = new ArrayList<String>();
                if (annotation.isSingleMemberAnnotation()) {
                    final SingleMemberAnnotation smAnn = (SingleMemberAnnotation)annotation;
                    values.add(smAnn.getValue().toString());
                }
                else if (annotation.isNormalAnnotation()) {
                    final NormalAnnotation normalAnn = (NormalAnnotation)annotation;
                    for (final Object value : normalAnn.values()) {
                        values.add(value.toString());
                    }
                }
                signature.append("@").append(annotation.resolveTypeBinding().getQualifiedName());
                if (!values.isEmpty()) {
                    signature.append("(");
                    boolean first = true;
                    for (final String string : values) {
                        if (!first) {
                            signature.append(", ");
                        }
                        first = false;
                        signature.append('\"').append(string).append('\"');
                    }
                    signature.append(")");
                }
                signature.append(" ");
            }
        }
        
        public static MethodDeclaration getRealMethodDeclarationNode(final IMethod sourceMethod, final CompilationUnit cuUnit) throws JavaModelException {
            final MethodDeclaration methodDeclarationNode = ASTNodeSearchUtil.getMethodDeclarationNode(sourceMethod, cuUnit);
            if (isGenerated((ASTNode)methodDeclarationNode)) {
                IType declaringType = sourceMethod.getDeclaringType();
                final Stack<IType> typeStack = new Stack<IType>();
                while (declaringType != null) {
                    typeStack.push(declaringType);
                    declaringType = declaringType.getDeclaringType();
                }
                final IType rootType = typeStack.pop();
                AbstractTypeDeclaration typeDeclaration;
                for (typeDeclaration = findTypeDeclaration(rootType, cuUnit.types()); !typeStack.isEmpty() && typeDeclaration != null; typeDeclaration = findTypeDeclaration(typeStack.pop(), typeDeclaration.bodyDeclarations())) {}
                if (typeStack.isEmpty() && typeDeclaration != null) {
                    final String methodName = sourceMethod.getElementName();
                    for (final Object declaration : typeDeclaration.bodyDeclarations()) {
                        if (declaration instanceof MethodDeclaration) {
                            final MethodDeclaration methodDeclaration = (MethodDeclaration)declaration;
                            if (methodDeclaration.getName().toString().equals(methodName)) {
                                return methodDeclaration;
                            }
                            continue;
                        }
                    }
                }
            }
            return methodDeclarationNode;
        }
        
        public static AbstractTypeDeclaration findTypeDeclaration(final IType searchType, final List<?> nodes) {
            for (final Object object : nodes) {
                if (object instanceof AbstractTypeDeclaration) {
                    final AbstractTypeDeclaration typeDeclaration = (AbstractTypeDeclaration)object;
                    if (typeDeclaration.getName().toString().equals(searchType.getElementName())) {
                        return typeDeclaration;
                    }
                    continue;
                }
            }
            return null;
        }
        
        public static int getSourceEndFixed(final int sourceEnd, final org.eclipse.jdt.internal.compiler.ast.ASTNode node) throws Exception {
            if (sourceEnd == -1) {
                final org.eclipse.jdt.internal.compiler.ast.ASTNode object = (org.eclipse.jdt.internal.compiler.ast.ASTNode)node.getClass().getField("$generatedBy").get(node);
                if (object != null) {
                    return object.sourceEnd;
                }
            }
            return sourceEnd;
        }
        
        public static int fixRetrieveStartingCatchPosition(final int original, final int start) {
            return (original == -1) ? start : original;
        }
        
        public static int fixRetrieveIdentifierEndPosition(final int original, final int end) {
            return (original == -1) ? end : original;
        }
        
        public static int fixRetrieveEllipsisStartPosition(final int original, final int end) {
            return (original == -1) ? end : original;
        }
        
        public static int fixRetrieveRightBraceOrSemiColonPosition(final int original, final int end) {
            return (original == -1) ? end : original;
        }
        
        public static int fixRetrieveRightBraceOrSemiColonPosition(final int retVal, final AbstractMethodDeclaration amd) {
            if (retVal != -1 || amd == null) {
                return retVal;
            }
            final boolean isGenerated = EclipseAugments.ASTNode_generatedBy.get((Object)amd) != null;
            if (isGenerated) {
                return amd.declarationSourceEnd;
            }
            return -1;
        }
        
        public static int fixRetrieveRightBraceOrSemiColonPosition(final int retVal, final FieldDeclaration fd) {
            if (retVal != -1 || fd == null) {
                return retVal;
            }
            final boolean isGenerated = EclipseAugments.ASTNode_generatedBy.get((Object)fd) != null;
            if (isGenerated) {
                return fd.declarationSourceEnd;
            }
            return -1;
        }
        
        public static boolean checkBit24(final Object node) throws Exception {
            final int bits = (int)node.getClass().getField("bits").get(node);
            return (bits & 0x800000) != 0x0;
        }
        
        public static boolean skipRewritingGeneratedNodes(final ASTNode node) throws Exception {
            return (boolean)node.getClass().getField("$isGenerated").get(node);
        }
        
        public static void setIsGeneratedFlag(final ASTNode domNode, final org.eclipse.jdt.internal.compiler.ast.ASTNode internalNode) throws Exception {
            if (internalNode == null || domNode == null) {
                return;
            }
            final boolean isGenerated = EclipseAugments.ASTNode_generatedBy.get((Object)internalNode) != null;
            if (isGenerated) {
                domNode.getClass().getField("$isGenerated").set(domNode, true);
            }
        }
        
        public static void setIsGeneratedFlagForName(final Name name, final Object internalNode) throws Exception {
            if (internalNode instanceof org.eclipse.jdt.internal.compiler.ast.ASTNode) {
                final boolean isGenerated = EclipseAugments.ASTNode_generatedBy.get((Object)internalNode) != null;
                if (isGenerated) {
                    name.getClass().getField("$isGenerated").set(name, true);
                }
            }
        }
        
        public static RewriteEvent[] listRewriteHandleGeneratedMethods(final RewriteEvent parent) {
            final RewriteEvent[] children = parent.getChildren();
            final List<RewriteEvent> newChildren = new ArrayList<RewriteEvent>();
            final List<RewriteEvent> modifiedChildren = new ArrayList<RewriteEvent>();
            for (int i = 0; i < children.length; ++i) {
                final RewriteEvent child = children[i];
                final boolean isGenerated = isGenerated((ASTNode)child.getOriginalValue());
                if (isGenerated) {
                    final boolean isReplacedOrRemoved = child.getChangeKind() == 4 || child.getChangeKind() == 2;
                    final boolean convertingFromMethod = child.getOriginalValue() instanceof MethodDeclaration;
                    if (isReplacedOrRemoved && convertingFromMethod && child.getNewValue() != null) {
                        modifiedChildren.add((RewriteEvent)new NodeRewriteEvent((Object)null, child.getNewValue()));
                    }
                }
                else {
                    newChildren.add(child);
                }
            }
            newChildren.addAll(modifiedChildren);
            return newChildren.toArray(new RewriteEvent[newChildren.size()]);
        }
        
        public static int getTokenEndOffsetFixed(final TokenScanner scanner, final int token, final int startOffset, final Object domNode) throws CoreException {
            boolean isGenerated = false;
            try {
                isGenerated = (boolean)domNode.getClass().getField("$isGenerated").get(domNode);
            }
            catch (Exception ex) {}
            if (isGenerated) {
                return -1;
            }
            return scanner.getTokenEndOffset(token, startOffset);
        }
        
        public static IMethod[] removeGeneratedMethods(final IMethod[] methods) throws Exception {
            final List<IMethod> result = new ArrayList<IMethod>();
            for (final IMethod m : methods) {
                if (m.getNameRange().getLength() > 0 && !m.getNameRange().equals(m.getSourceRange())) {
                    result.add(m);
                }
            }
            return (result.size() == methods.length) ? methods : result.toArray(new IMethod[result.size()]);
        }
        
        public static SearchMatch[] removeGenerated(final SearchMatch[] returnValue) {
            final List<SearchMatch> result = new ArrayList<SearchMatch>();
            for (int j = 0; j < returnValue.length; ++j) {
                final SearchMatch searchResult = returnValue[j];
                if (searchResult.getElement() instanceof IField) {
                    final IField field = (IField)searchResult.getElement();
                    final IAnnotation annotation = field.getAnnotation("Generated");
                    if (annotation != null) {
                        continue;
                    }
                }
                result.add(searchResult);
            }
            return result.toArray(new SearchMatch[result.size()]);
        }
        
        public static SearchResultGroup[] createFakeSearchResult(final SearchResultGroup[] returnValue, final Object processor) throws Exception {
            if (returnValue == null || returnValue.length == 0) {
                final Field declaredField = processor.getClass().getDeclaredField("fField");
                if (declaredField != null) {
                    declaredField.setAccessible(true);
                    final SourceField fField = (SourceField)declaredField.get(processor);
                    final IAnnotation dataAnnotation = fField.getDeclaringType().getAnnotation("Data");
                    if (dataAnnotation != null) {
                        return new SearchResultGroup[] { new SearchResultGroup((IResource)null, new SearchMatch[1]) };
                    }
                }
            }
            return returnValue;
        }
        
        public static SimpleName[] removeGeneratedSimpleNames(final SimpleName[] in) throws Exception {
            final Field f = SimpleName.class.getField("$isGenerated");
            int count = 0;
            for (int i = 0; i < in.length; ++i) {
                if (in[i] == null || !(boolean)f.get(in[i])) {
                    ++count;
                }
            }
            if (count == in.length) {
                return in;
            }
            final SimpleName[] newSimpleNames = new SimpleName[count];
            count = 0;
            for (int j = 0; j < in.length; ++j) {
                if (in[j] == null || !(boolean)f.get(in[j])) {
                    newSimpleNames[count++] = in[j];
                }
            }
            return newSimpleNames;
        }
        
        public static org.eclipse.jdt.internal.compiler.ast.Annotation[] convertAnnotations(final org.eclipse.jdt.internal.compiler.ast.Annotation[] out, final IAnnotatable annotatable) {
            IAnnotation[] in;
            try {
                in = annotatable.getAnnotations();
            }
            catch (Exception e) {
                return out;
            }
            if (out == null) {
                return null;
            }
            int toWrite = 0;
            for (int idx = 0; idx < out.length; ++idx) {
                final String oName = new String(out[idx].type.getLastToken());
                boolean found = false;
                for (final IAnnotation i : in) {
                    String name = i.getElementName();
                    final int li = name.lastIndexOf(46);
                    if (li > -1) {
                        name = name.substring(li + 1);
                    }
                    if (name.equals(oName)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    out[idx] = null;
                }
                else {
                    ++toWrite;
                }
            }
            org.eclipse.jdt.internal.compiler.ast.Annotation[] replace = out;
            if (toWrite < out.length) {
                replace = new org.eclipse.jdt.internal.compiler.ast.Annotation[toWrite];
                int idx2 = 0;
                for (int j = 0; j < out.length; ++j) {
                    if (out[j] != null) {
                        replace[idx2++] = out[j];
                    }
                }
            }
            return replace;
        }
    }
}
