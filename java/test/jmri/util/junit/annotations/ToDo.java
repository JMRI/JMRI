package jmri.util.junit.annotations;

import java.lang.annotation.*;
import org.junit.Ignore;

/**
 * Annotation for test denoting work that needs to be done to a specific test.  
 * to be fully functional.  This annotation may be used with {@link Ignore}.
 * <p>
 *
 * @author Paul Bender Copyright 2018
 */

@Retention(RetentionPolicy.CLASS)  // For access by SpotBugs et al 
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Documented
@Inherited
public @interface ToDo {
    /**
     * The optional reason why the test needs work.
     */
    String value() default "";
}

