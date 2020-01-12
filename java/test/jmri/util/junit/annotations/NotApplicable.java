package jmri.util.junit.annotations;

import java.lang.annotation.*;
import org.junit.Ignore;

/**
 * Annotation denoting that an overriden test is not applicable to a particular
 * class under test.  This be used instead of {@link Ignore} with an empty 
 * bodied test.
 * <p>
 *
 * @author Paul Bender Copyright 2018
 */

@Retention(RetentionPolicy.CLASS)  // For access by SpotBugs et al 
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Documented
@Inherited
public @interface NotApplicable {
    /**
     * The optional reason why the test is not applicable.
     */
    String value() default "";
}

