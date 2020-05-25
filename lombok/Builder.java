package lombok;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR })
@Retention(RetentionPolicy.SOURCE)
public @interface Builder {
    String builderMethodName() default "builder";
    
    String buildMethodName() default "build";
    
    String builderClassName() default "";
    
    boolean toBuilder() default false;
    
    @Target({ ElementType.FIELD, ElementType.PARAMETER })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ObtainVia {
        String field() default "";
        
        String method() default "";
        
        boolean isStatic() default false;
    }
}
