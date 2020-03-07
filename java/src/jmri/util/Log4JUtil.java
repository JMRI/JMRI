package jmri.util;

import apps.SystemConsole;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.util.exceptionhandler.UncaughtExceptionHandler;

import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.RollingFileAppender;
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
 */
public class Log4JUtil {

    private static boolean log4JSetUp = false;
    private static final String jmriLog = "****** JMRI log *******";
    private static final Logger log = LoggerFactory.getLogger(Log4JUtil.class);

    /**
     * Emit a particular WARNING-level message just once.
     * @return true if the log was emitted this time
     */
    // Goal is to be lightweight and fast; this will only be used in a few places,
    // and only those should appear in data structure.
    static public boolean warnOnce(@Nonnull Logger logger, @Nonnull String msg, Object... args) {
        // the  Map<String, Boolean> is just being checked for existence; it's never False
        Map<String, Boolean> loggerMap = warnedOnce.get(logger);
        if (loggerMap == null) {  // if it exists, there was a prior warning given
            loggerMap = new HashMap<>();
            warnedOnce.put(logger, loggerMap);
        } else {
            if (Boolean.TRUE.equals(loggerMap.get(msg))) return false;
        }
        warnOnceHasWarned = true;
        loggerMap.put(msg, Boolean.TRUE);
        logger.warn(msg, args);
        return true;
    }
    static private Map<Logger, Map<String, Boolean>> warnedOnce = new HashMap<>();
    static private boolean warnOnceHasWarned = false;
    
    /**
     * Restart the "once" part of {@link #warnOnce} so that the 
     * nextInvocation will log, even if it already has.
     * <p>
     * Should only be used by test code. We denote this
     * by marking it deprecated, but we don't intend to remove it.
     * @deprecated - do not remove
     */
    @Deprecated // do not remove
    static public void restartWarnOnce() {
        // be a bit more efficient
        if (warnOnceHasWarned) {
            warnedOnce = new HashMap<>();
            warnOnceHasWarned = false;
        }
    }
    
    /**
     * Warn that a deprecated method has been invoked.
     * Can also be used to warn of some deprecated condition, i.e.
     * obsolete-format input data.
     * <p>
     * Thie logging is turned off by default during testing to
     * simplify updating tests when warnings are added.
     */
     static public void deprecationWarning(@Nonnull Logger logger, @Nonnull String methodName) {
        if (logDeprecations) {
            warnOnce(logger, "{} is deprecated, please remove references to it", methodName, shortenStacktrace(new Exception("traceback")));
        }
     }
     
    static private boolean logDeprecations = true;
    
    /**
     * Control logging of deprecation warnings.
     * <p> 
     * Should only be used by test code. We denote this
     * by marking it deprecated, but we don't intend to remove it.
     * (Might have to if we start removing deprecated references from test code)
     * @deprecated - do not remove
     */
    @Deprecated // do not remove
    public static void setDeprecatedLogging(boolean log) {logDeprecations = log;}

    /**
     * Determine whether deprecation warnings are logged.
     * <p> 
     * Should only be used by test code. We denote this
     * by marking it deprecated, but we don't intend to remove it.
     * (Might have to if we start removing deprecated references from test code)
     * @deprecated - do not remove
     */
    @Deprecated // do not remove
    public static boolean getDeprecatedLogging() { return logDeprecations;}
     
    /**
     * Initialize logging from a default control file.
     * <p>
     * Primary functions:
     * <ul>
     * <li>Initialize the JMRI System Console
     * <li>Set up the slf4j j.u.logging to log4J bridge
     * <li>Start log4j
     * <li>Initialize some default exception handlers (to feed the logs?)
     * </ul>
     */
    static public void initLogging() {
        initLogging(System.getProperty("jmri.log", "default.lcf"));
    }

    /**
     * Initialize logging, specifying a control file.
     * <p>
     * Generally, only used for unit testing. Much better to use allow this
     * class to find the control file using a set of conventions.
     *
     * @param controlfile the logging control file
     */
    static public void initLogging(@Nonnull String controlfile) {
        initLog4J(controlfile);
    }

    /**
     * Initialize Log4J.
     * <p>
     * Use the logging control file specified in the <i>jmri.log</i> property
     * or, if none, the default.lcf file. If the file is absolute and cannot be
     * found, look for the file first in the settings directory and then in the
     * installation directory.
     *
     * @param logFile the logging control file
     * @see jmri.util.FileUtil#getPreferencesPath()
     * @see jmri.util.FileUtil#getProgramPath()
     */
    static void initLog4J(@Nonnull String logFile) {
        if (log4JSetUp) {
            log.debug("initLog4J already initialized!");
            return;
        }
        // Initialise JMRI System Console
        // Need to do this before initialising log4j so that the new
        // stdout and stderr streams are set up and usable by the ConsoleAppender
        SystemConsole.create();
        log4JSetUp = true;

        // initialize the java.util.logging to log4j bridge
        initializeJavaUtilLogging();

        // initialize log4j - from logging control file (lcf) only
        try {
            if (new File(logFile).isAbsolute() && new File(logFile).canRead()) {
                configureLogging(logFile);
            } else if (new File(FileUtil.getPreferencesPath() + logFile).canRead()) {
                configureLogging(FileUtil.getPreferencesPath() + logFile);
            } else if (new File(FileUtil.getProgramPath() + logFile).canRead()) {
                configureLogging(FileUtil.getProgramPath() + logFile);
            } else {
                BasicConfigurator.configure();
                org.apache.log4j.Logger.getRootLogger().setLevel(Level.WARN);
            }
        } catch (java.lang.NoSuchMethodError e) {
            log.error("Exception starting logging", e);
        } catch (IOException ex) {
            BasicConfigurator.configure();
            org.apache.log4j.Logger.getRootLogger().setLevel(Level.WARN);
        }
        // install default exception handler so uncaught exceptions are logged, not printed
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    }

    static void initializeJavaUtilLogging() {
        // Optionally remove existing handlers attached to j.u.l root logger
        org.slf4j.bridge.SLF4JBridgeHandler.removeHandlersForRootLogger();  // (since SLF4J 1.6.5)

        // add SLF4JBridgeHandler to j.u.l's root logger, should be done once during
        // the initialization phase of your application
        org.slf4j.bridge.SLF4JBridgeHandler.install();
    }

    @SuppressWarnings("unchecked")
    static public @Nonnull String startupInfo(@Nonnull String program) {
        log.info(jmriLog);
        Enumeration<org.apache.log4j.Logger> e = org.apache.log4j.Logger.getRootLogger().getAllAppenders();
        while (e.hasMoreElements()) {
            Appender a = (Appender) e.nextElement();
            if (a instanceof RollingFileAppender) {
                log.info("This log is appended to file: " + ((RollingFileAppender) a).getFile());
            } else if (a instanceof FileAppender) {
                log.info("This log is stored in file: " + ((FileAppender) a).getFile());
            }
        }
        return (program + " version " + jmri.Version.name()
                + " starts under Java " + System.getProperty("java.version", "<unknown>")
                + " on " + System.getProperty("os.name", "<unknown>")
                + " " + System.getProperty("os.arch", "<unknown>")
                + " v" + System.getProperty("os.version", "<unknown>")
                + " at " + (new java.util.Date()));
    }

    /**
     * Configure Log4J using the specified properties file.
     * <p>
     * This method sets the system property <i>jmri.log.path</i> to the JMRI
     * preferences directory if not specified.
     *
     * @see jmri.util.FileUtil#getPreferencesPath()
     */
    static private void configureLogging(@Nonnull String configFile) throws IOException {
        Properties p = new Properties();
        try (FileInputStream f = new FileInputStream(configFile)) {
            p.load(f);
        }

        if (System.getProperty("jmri.log.path") == null || p.getProperty("jmri.log.path") == null) {
            System.setProperty("jmri.log.path", FileUtil.getPreferencesPath() + "log" + File.separator);
            p.put("jmri.log.path", System.getProperty("jmri.log.path"));
        }
        File logDir = new File(p.getProperty("jmri.log.path"));
        // ensure the logging directory exists
        // if it's not writable, the console will get the error from log4j, so
        // we don't need to explictly test for that here, just make sure the
        // directory is created if need be.
        if (!logDir.exists()) {
            Files.createDirectories(logDir.toPath());
        }
        PropertyConfigurator.configure(p);
    }

    /**
     * Shorten this stack trace in a Throwable to start with the first JMRI method.  
     * <p>
     * If you then pass it to 
     * Log4J for logging, it'll take up less space.
     * @param t The Throwable to truncate and return
     * @return The original object with truncated stack trace
     */
    public  @Nonnull static <T extends Throwable> T shortenStacktrace(@Nonnull T t) {
        StackTraceElement[]	originalTrace = t.getStackTrace();
        int i;
        for (i = originalTrace.length-1; i>0; i--) { // search from deepest
            String name = originalTrace[i].getClassName();
            if (name.equals("jmri.util.junit.TestClassMainMethod")) continue; // special case to ignore high up in stack
            if (name.equals("apps.tests.AllTest")) continue;                 // special case to ignore high up in stack
            if (name.equals("jmri.HeadLessTest")) continue;                 // special case to ignore high up in stack
            if (name.startsWith("jmri") || name.startsWith("apps")) break;  // keep those
        }
        return shortenStacktrace(t, i+1);
    }

    /**
     * Shorten this stack trace in a Throwable to a fixed length.
     * <p>
     * If you then pass it to 
     * Log4J for logging, it'll take up less space.
     * @param t The Throwable to truncate and return
     * @param len The number of stack trace entries to keep.
     * @return The original object with truncated stack trace
     */
    public  @Nonnull static <T extends Throwable> T shortenStacktrace(@Nonnull T t, int len) {
        StackTraceElement[]	originalTrace = t.getStackTrace();
        int newLen = Math.min(len, originalTrace.length);
        StackTraceElement[] newTrace = new StackTraceElement[newLen];
        for (int i = 0; i < newLen; i++) newTrace[i] = originalTrace[i];
        t.setStackTrace(newTrace);
        return t;
    }
}
