package apps.util;

import java.awt.GraphicsEnvironment;

import apps.SystemConsole;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.util.FileUtil;
import jmri.util.exceptionhandler.UncaughtExceptionHandler;

import org.apache.log4j.*;
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
 * @author Randall Wood Copyright 2014, 2020
 */
public class Log4JUtil {

    private static final String LOG_HEADER = "****** JMRI log *******";
    private static final Logger log = LoggerFactory.getLogger(Log4JUtil.class);

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
        if (LogManager.getRootLogger().getAllAppenders().hasMoreElements()) {
            log.debug("initLog4J already initialized!");
            return;
        }
        // Initialise JMRI System Console
        // Need to do this before initialising log4j so that the new
        // stdout and stderr streams are set up and usable by the ConsoleAppender
        if (!GraphicsEnvironment.isHeadless()) {
            SystemConsole.create();
        }

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
    @Nonnull
    static public String startupInfo(@Nonnull String program) {
        log.info(LOG_HEADER);
        Enumeration<org.apache.log4j.Logger> e = org.apache.log4j.Logger.getRootLogger().getAllAppenders();
        while (e.hasMoreElements()) {
            Appender a = (Appender) e.nextElement();
            if (a instanceof RollingFileAppender) {
                log.info("This log is appended to file: {}", ((FileAppender) a).getFile());
            } else if (a instanceof FileAppender) {
                log.info("This log is stored in file: {}", ((FileAppender) a).getFile());
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

}
