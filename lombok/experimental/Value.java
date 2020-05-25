package lombok.experimental;

import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.SOURCE)
@Deprecated
public @interface Value {
    String staticConstructor() default "";
}
