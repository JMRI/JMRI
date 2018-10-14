package jmri;

import java.lang.annotation.*;

/**
 * Annotation denoting that a method must be called on the GUI thread.
 * <p>
 * For more information on JMRI threading conventions, see the 
 * <a href="http://jmri.org/help/en/html/doc/Technical/Threads.shtml">JMRI Threading docoumentation page</a>.
 *
 * @author Bob Jacobsen  Copyright 2018
 */

@Retention(RetentionPolicy.SOURCE)  // Currently, just in the source code, not .class files or runtime
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Documented
@Inherited

public @interface InvokeOnGuiThread {}

