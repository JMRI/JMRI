package jmri.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.*;

import javax.annotation.Nonnull;

import org.slf4j.Logger;

/**
 * Basic utilities for logging special messages.
 *
 * @author Randall Wood Copyright 2020
 */
public class LoggingUtil {

    protected static Map<Logger, Set<String>> warnedOnce = new HashMap<>();
    protected static boolean logDeprecations = true;

    /**
     * Emit a particular WARNING-level message just once.
     * <p>
     * Goal is to be lightweight and fast; this will only be used in a few
     * places, and only those should appear in data structure.
     *
     * @param logger the source of the warning
     * @param msg    warning message
     * @param args   message arguments
     * @return true if the log was emitted this time
     */
    @SuppressFBWarnings(value = "SLF4J_UNKNOWN_ARRAY", justification = "Passing varargs array through")
    public static boolean warnOnce(@Nonnull Logger logger, @Nonnull String msg, Object... args) {
        Set<String> loggerSet = warnedOnce.computeIfAbsent(logger, l -> new HashSet<>());
        // if it exists, there was a prior warning given
        if (loggerSet.contains(msg)) {
            return false;
        }
        loggerSet.add(msg);
        logger.warn(msg, args);
        return true;
    }

    /**
     * Warn that a deprecated method has been invoked.
     * <p>
     * Can also be used to warn of some deprecated condition, i.e.
     * obsolete-format input data.
     * <p>
     * The logging is turned off by default during testing to simplify updating
     * tests when warnings are added.
     *
     * @param logger     The Logger to warn.
     * @param methodName method name.
     */
    public static void deprecationWarning(@Nonnull Logger logger, @Nonnull String methodName) {
        if (logDeprecations) {
            warnOnce(logger, "{} is deprecated, please remove references to it", methodName, shortenStacktrace(new Exception("traceback")));
        }
    }

    /**
     * Shorten a stack trace to start with the first JMRI method.
     * <p>
     * When logged, the stack trace will be more focused.
     *
     * @param <T> the type of Throwable
     * @param t   the Throwable containing the stack trace to truncate
     * @return t with truncated stack trace
     */
    @Nonnull
    public static <T extends Throwable> T shortenStacktrace(@Nonnull T t) {
        StackTraceElement[] originalTrace = t.getStackTrace();
        int i;
        for (i = originalTrace.length - 1; i > 0; i--) {
            // search from deepest
            String name = originalTrace[i].getClassName();
            if (name.equals("jmri.util.junit.TestClassMainMethod")) {
                continue; // special case to ignore high up in stack
            }
            if (name.equals("apps.tests.AllTest")) {
                continue; // special case to ignore high up in stack
            }
            if (name.equals("jmri.HeadLessTest")) {
                continue; // special case to ignore high up in stack
            }
            if (name.startsWith("jmri") || name.startsWith("apps")) {
                break; // keep those
            }
        }
        return shortenStacktrace(t, i + 1);
    }

    /**
     * Shorten a stack trace to a fixed length.
     * <p>
     * When logged, the stack trace will be more focused.
     *
     * @param <T> the type of Throwable
     * @param t   the Throwable containing the stack trace to truncate
     * @param len length of stack trace to retain
     * @return t with truncated stack trace
     */
    @Nonnull
    public static <T extends Throwable> T shortenStacktrace(@Nonnull T t, int len) {
        StackTraceElement[] originalTrace = t.getStackTrace();
        int newLen = Math.min(len, originalTrace.length);
        StackTraceElement[] newTrace = new StackTraceElement[newLen];
        System.arraycopy(originalTrace, 0, newTrace, 0, newLen);
        t.setStackTrace(newTrace);
        return t;
    }

}
