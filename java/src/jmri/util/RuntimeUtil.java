package jmri.util;

/**
 *
 * <P>
 * We needed a place to put code to Java 2 Swing functionality on a Java 1.1.8
 * system, or at least try to fake it.
 *
 * @author Paul Bender Copyright 2004
 */
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
