package jmri.util.junit.annotations;

import java.lang.annotation.*;

/**
 * Annotation denoting that an overriden test is not applicable to a particular
 * class under test. This can be used instead of
 * {@link org.junit.jupiter.api.Disabled} with an empty bodied test.
 * <p>
 * Using NotApplicable will prevent execution of code within the annotated method.
 * As with {@link org.junit.jupiter.api.Disabled}, the BeforeEach and AfterEach
 * methods within the test class will not be called for NotApplicable tests,
 * speeding up test runs.
 *
 * @author Paul Bender Copyright 2018
 */

@Retention(RetentionPolicy.RUNTIME)  // For access by JUnit
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Documented
@Inherited
@org.junit.jupiter.api.extension.ExtendWith(NotApplicableExecutionCondition.class)
public @interface NotApplicable {
    /**
     * The optional reason why the test is not applicable.
     */
    String value() default "";
}

