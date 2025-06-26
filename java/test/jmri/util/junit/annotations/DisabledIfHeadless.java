package jmri.util.junit.annotations;

import java.lang.annotation.*;

/**
 * Annotation denoting that tests within a class or test method
 * should not be run if headless.
 * @author Steve Young Copyright 2024
 */
@Retention(RetentionPolicy.RUNTIME)  // For access by JUnit
@Target({ElementType.METHOD, ElementType.TYPE}) // method and class
@Documented
@Inherited
@org.junit.jupiter.api.extension.ExtendWith(DisabledIfHeadlessExecutionCondition.class)
public @interface DisabledIfHeadless {
    /**
     * The optional reason why the test is not run when headless.
     */
    String value() default "";

}
