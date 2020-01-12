package jmri;

import java.lang.annotation.*;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Annotation denoting that a method or constructor may be called on any thread.
 * <p>
 * This annotation implies a method is {@link ThreadSafe}, but says nothing 
 * about whether or not a class is ThreadSafe. 
 * <p>
 * For more information on JMRI threading conventions, see the 
 * <a href="http://jmri.org/help/en/html/doc/Technical/Threads.shtml">JMRI Threading docoumentation page</a>.
 *
 * @author Bob Jacobsen  Copyright 2018
 * @author Paul Bender Copyright 2018
 */

@Retention(RetentionPolicy.CLASS)  // For access by SpotBugs et al 
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Documented
@Inherited

public @interface InvokeOnAnyThread {}

