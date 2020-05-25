package lombok.launch;

import org.mapstruct.ap.spi.*;
import javax.lang.model.type.*;
import java.util.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.annotation.processing.*;

class AnnotationProcessorHider
{
    public static class AstModificationNotifier implements AstModifyingAnnotationProcessor
    {
        public boolean isTypeComplete(final TypeMirror type) {
            return System.getProperty("lombok.disable") != null || AstModificationNotifierData.lombokInvoked;
        }
    }
    
    static class AstModificationNotifierData
    {
        static volatile boolean lombokInvoked;
        
        static {
            AstModificationNotifierData.lombokInvoked = false;
        }
    }
    
    public static class AnnotationProcessor extends AbstractProcessor
    {
        private final AbstractProcessor instance;
        
        public AnnotationProcessor() {
            this.instance = createWrappedInstance();
        }
        
        @Override
        public Set<String> getSupportedOptions() {
            return this.instance.getSupportedOptions();
        }
        
        @Override
        public Set<String> getSupportedAnnotationTypes() {
            return this.instance.getSupportedAnnotationTypes();
        }
        
        @Override
        public SourceVersion getSupportedSourceVersion() {
            return this.instance.getSupportedSourceVersion();
        }
        
        @Override
        public void init(final ProcessingEnvironment processingEnv) {
            AstModificationNotifierData.lombokInvoked = true;
            this.instance.init(processingEnv);
            super.init(processingEnv);
        }
        
        @Override
        public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
            return this.instance.process(annotations, roundEnv);
        }
        
        @Override
        public Iterable<? extends Completion> getCompletions(final Element element, final AnnotationMirror annotation, final ExecutableElement member, final String userText) {
            return this.instance.getCompletions(element, annotation, member, userText);
        }
        
        private static AbstractProcessor createWrappedInstance() {
            final ClassLoader cl = Main.createShadowClassLoader();
            try {
                final Class<?> mc = cl.loadClass("lombok.core.AnnotationProcessor");
                return (AbstractProcessor)mc.newInstance();
            }
            catch (Throwable t) {
                if (t instanceof Error) {
                    throw (Error)t;
                }
                if (t instanceof RuntimeException) {
                    throw (RuntimeException)t;
                }
                throw new RuntimeException(t);
            }
        }
    }
    
    @SupportedAnnotationTypes({ "lombok.*" })
    public static class ClaimingProcessor extends AbstractProcessor
    {
        @Override
        public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
            return true;
        }
        
        @Override
        public SourceVersion getSupportedSourceVersion() {
            return SourceVersion.latest();
        }
    }
}
