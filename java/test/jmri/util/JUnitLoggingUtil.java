package jmri.util;

import java.util.*;

/**
 * Basic utilities for logging special messages in tests.
 *
 * @author Randall Wood Copyright 2020
 */
public class JUnitLoggingUtil extends LoggingUtil {

    /**
     * Restart the "once" part of {@link #warnOnce} so that the nextInvocation
     * will log, even if it already has.
     */
    public static void restartWarnOnce() {
        warnedOnce = new HashMap<>();
    }

    /**
     * Control logging of deprecation warnings.
     * <p>
     * Should only be used by test code. We denote this by marking it
     * deprecated, but we don't intend to remove it. (Might have to if we start
     * removing deprecated references from test code)
     *
     * @param log true to log deprecations, else false.
     */
    public static void setDeprecatedLogging(boolean log) {
        logDeprecations = log;
    }

    /**
     * Determine whether deprecation warnings are logged.
     * <p>
     * Should only be used by test code. We denote this by marking it
     * deprecated, but we don't intend to remove it. (Might have to if we start
     * removing deprecated references from test code)
     *
     * @return true if deprecation warnings are logged.
     */
    public static boolean getDeprecatedLogging() {
        return logDeprecations;
    }

}
