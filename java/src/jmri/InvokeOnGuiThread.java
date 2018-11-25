package jmri;

import java.lang.annotation.*;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * Annotation denoting that a method or constructor must be called on the GUI (i.e. Swing) thread.
 * <p>
 * A class with one or more methods that must be called on a particular thread is by definition 
 * {@link NotThreadSafe}, so please include the @NotThreadSafe annotation on the class.
 * <p>
 * For more information on JMRI threading conventions, see the 
 * <a href="http://jmri.org/help/en/html/doc/Technical/Threads.shtml">JMRI Threading docoumentation page</a>.
 *
 * @author Bob Jacobsen  Copyright 2018
 */

@Retention(RetentionPolicy.CLASS)  // For access by SpotBugs et al 
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Documented
@Inherited

public @interface InvokeOnGuiThread {}

