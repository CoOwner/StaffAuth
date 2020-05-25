package lombok.experimental;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR })
@Retention(RetentionPolicy.SOURCE)
@Deprecated
public @interface Builder {
    String builderMethodName() default "builder";
    
    String buildMethodName() default "build";
    
    String builderClassName() default "";
    
    boolean fluent() default true;
    
    boolean chain() default true;
}
