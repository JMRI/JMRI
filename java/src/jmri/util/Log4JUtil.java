package jmri.util;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common utility methods for working with Log4J.
 * <p>
 * Two system properties influence how logging is configured in JMRI:
 * <dl>
 * <dt>jmri.log</dt><dd>The logging control file. If this file is not an
 * absolute path, this file is searched for in the following order:<ol>
 * <li>JMRI settings directory</li>
 * <li>JMRI installation (program) directory</li>
 * </ol>
 * If this property is not specified, the logging control file
 * <i>default.lcf</i> is used, following the above search order to find it.
 * </dd>
 * <dt>jmri.log.path</dt><dd>The directory for storing logs. If not specified,
 * logs are stored in the JMRI preferences directory.</dd>
 * </dl>
 *
 * @author Bob Jacobsen Copyright 2009, 2010
 * @author Randall Wood Copyright 2014
 * @deprecated since 4.22.1; split into {@link LoggingUtil},
 * {@link apps.util.Log4JUtil}, and jmri.util.JUnitLoggingUtil (for tests only)
 */
@Deprecated
public class Log4JUtil {

    private static final Logger log = LoggerFactory.getLogger(Log4JUtil.class);

    /**
     * Emit a particular WARNING-level message just once.
     * <p>
     * Goal is to be lightweight and fast; this will only be used in a few
     * places, and only those should appear in data structure.
     *
     * @param logger The Logger to warn.
     * @param msg    Message.
     * @param args   Message Arguments.
     * @return true if the log was emitted this time
     * @deprecated since 4.22.1; use {@link LoggingUtil#warnedOnce} instead
     */
    @Deprecated
    static public boolean warnOnce(@Nonnull Logger logger, @Nonnull String msg, Object... args) {
        LoggingUtil.deprecationWarning(log, "warnOnce");
        return LoggingUtil.warnOnce(logger, msg, args);
    }

    /**
     * Restart the "once" part of {@link #warnOnce} so that the nextInvocation
     * will log, even if it already has.
     * <p>
     * Should only be used by test code.
     *
     * @deprecated since 4.22.1; use jmri.util.JUnitLoggingUtil#restartWarnOnce
     */
    @Deprecated
    static public void restartWarnOnce() {
        LoggingUtil.deprecationWarning(log, "restartWarnOnce");
        LoggingUtil.warnOnce(log, "restartWarnOnce has no effect");
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
     * @deprecated since 4.22.1; use
     * {@link LoggingUtil#deprecationWarning(Logger, String)} instead
     */
    @Deprecated
    static public void deprecationWarning(@Nonnull Logger logger, @Nonnull String methodName) {
        LoggingUtil.deprecationWarning(log, "deprecationWarning");
        LoggingUtil.deprecationWarning(logger, methodName);
    }

    /**
     * Control logging of deprecation warnings.
     * <p>
     * Should only be used by test code.
     *
     * @param log true to log deprecations, else false.
     * @deprecated since 4.22.1; use
     * jmri.util.JUnitLoggingUtil#setDeprecatedLogging instead
     */
    @Deprecated
    public static void setDeprecatedLogging(boolean log) {
        LoggingUtil.deprecationWarning(Log4JUtil.log, "setDeprecatedLogging");
        LoggingUtil.warnOnce(Log4JUtil.log, "setDeprecatedLogging has no effect");
    }

    /**
     * Determine whether deprecation warnings are logged.
     * <p>
     * Should only be used by test code.
     *
     * @return true if deprecation warnings are logged.
     * @deprecated since 4.22.1; use
     * jmri.util.JUnitLoggingUtil#getDeprecatedLogging instead
     */
    @Deprecated
    public static boolean getDeprecatedLogging() {
        LoggingUtil.deprecationWarning(log, "getDeprecatedLogging");
        LoggingUtil.warnOnce(log, "getDeprecatedLogging always returns true");
        return true;
    }

    /**
     * Initialize logging from a default control file.
     *
     * @deprecated since 4.22.1; use {@link apps.util.Log4JUtil#initLogging()}
     * instead
     */
    @Deprecated
    static public void initLogging() {
        LoggingUtil.deprecationWarning(log, "initLogging");
        LoggingUtil.warnOnce(log, "initLogging has no effect");
    }

    /**
     * Initialize logging, specifying a control file.
     * <p>
     * Generally, only used for unit testing. Much better to use allow this
     * class to find the control file using a set of conventions.
     *
     * @param controlfile the logging control file
     * @deprecated since 4.22.1; use
     * {@link apps.util.Log4JUtil#initLogging(String)} instead
     */
    @Deprecated
    static public void initLogging(@Nonnull String controlfile) {
        LoggingUtil.deprecationWarning(log, "initLogging");
        LoggingUtil.warnOnce(log, "initLogging has no effect");
    }

    /**
     * Return a block of OS and JVM information with the JMRI application
     * version.
     *
     * @param program name to be included
     * @return the information block
     * @deprecated since 4.22.1; use
     * {@link apps.util.Log4JUtil#startupInfo(String)} instead
     */
    @Deprecated
    @Nonnull
    static public String startupInfo(@Nonnull String program) {
        LoggingUtil.deprecationWarning(log, "startupInfo");
        return (program + " version " + jmri.Version.name()
                + " starts under Java " + System.getProperty("java.version", "<unknown>")
                + " on " + System.getProperty("os.name", "<unknown>")
                + " " + System.getProperty("os.arch", "<unknown>")
                + " v" + System.getProperty("os.version", "<unknown>")
                + " at " + (new java.util.Date()));
    }

    /**
     * Shorten this stack trace in a Throwable to start with the first JMRI
     * method.
     * <p>
     * If you then pass it to Log4J for logging, it'll take up less space.
     *
     * @param <T> Throwable generic
     * @param t   The Throwable to truncate and return
     * @return The original object with truncated stack trace
     * @deprecated since 4.22.1; use
     * {@link LoggingUtil#shortenStacktrace(Throwable)} instead
     */
    @Deprecated
    public @Nonnull
    static <T extends Throwable> T shortenStacktrace(@Nonnull T t) {
        LoggingUtil.deprecationWarning(log, "shortenStackTrace");
        return LoggingUtil.shortenStacktrace(t);
    }

    /**
     * Shorten this stack trace in a Throwable to a fixed length.
     * <p>
     * If you then pass it to Log4J for logging, it'll take up less space.
     *
     * @param <T> Throwable generic
     * @param t   The Throwable to truncate and return
     * @param len The number of stack trace entries to keep.
     * @return The original object with truncated stack trace
     * @deprecated since 4.22.1; use
     * {@link LoggingUtil#shortenStacktrace(Throwable, int)} instead
     */
    @Deprecated
    @Nonnull
    public static <T extends Throwable> T shortenStacktrace(@Nonnull T t, int len) {
        LoggingUtil.deprecationWarning(log, "shortenStackTrace");
        return LoggingUtil.shortenStacktrace(t, len);
    }
}
