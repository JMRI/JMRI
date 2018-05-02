package jmri.util;

/**
 *
 * <P>
 * We needed a place to put code to Java 2 Swing functionality on a Java 1.1.8
 * system, or at least try to fake it.
 *
 * @author Paul Bender Copyright 2004
 * @deprecated since 4.5.4; add {@link jmri.ShutDownTask}s to the default
 * {@link jmri.ShutDownManager}, or if a ShutDownTask cannot be used, use
 * {@link java.lang.Runtime#addShutdownHook(java.lang.Thread)} directly
 */
@Deprecated
public class RuntimeUtil {

    static public void addShutdownHook(Thread Hook) {
        try {
            java.lang.Runtime.getRuntime().addShutdownHook(Hook);
        } catch (Throwable e) {
            // if addShutdownHook() doesn't exist in this 
            // version of the JVM, we'll just ignore this.
        }
    }
}
