package lombok.experimental;

import java.lang.annotation.*;

@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.SOURCE)
public @interface Delegate {
    Class<?>[] types() default {};
    
    Class<?>[] excludes() default {};
}
