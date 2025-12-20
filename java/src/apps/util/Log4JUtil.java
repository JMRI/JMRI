package apps.util;

import java.awt.GraphicsEnvironment;

import apps.SystemConsole;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.util.FileUtil;
import jmri.util.exceptionhandler.UncaughtExceptionHandler;
import jmri.util.swing.JmriJOptionPane;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.config.Configurator;

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
 * <i>default_lcf.xml</i> is used, following the above search order to find it.
 * </dd>
 * <dt>jmri.log.path</dt><dd>The directory for storing logs. If not specified,
 * logs are stored in the JMRI preferences directory.</dd>
 * </dl>
 * <p>
 * See also jmri.util.TestingLoggerConfiguration in the Test code for
 * Tests Logging Setup.
 * @author Bob Jacobsen Copyright 2009, 2010
 * @author Randall Wood Copyright 2014, 2020
 */
public class Log4JUtil {

    public static final String DEFAULT_LCF_NAME = "default_lcf.xml";
    public static final String SYS_PROP_LCF_LOCATION = "jmri.log";
    public static final String SYS_PROP_LOG_PATH =  "jmri.log.path";

    private static final String LOG_HEADER = "****** JMRI log *******";

    /**
     * Initialize logging from a default control file.
     * <p>
     * Primary functions:
     * <ul>
     * <li>Initialize the JMRI System Console.
     * <li>Set up the slf4j j.u.logging to log4J bridge.
     * <li>Start log4j.
     * <li>Initialize a default exception handler.
     * </ul>
     *
     */
    static public void initLogging() {
        initLogging(System.getProperty(SYS_PROP_LCF_LOCATION, DEFAULT_LCF_NAME));
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
     * or, if none, the default_lcf.xml file. If the file is absolute and cannot be
     * found, look for the file first in the settings directory and then in the
     * installation directory.
     *
     * @param logFile the logging control file
     * @see jmri.util.FileUtil#getPreferencesPath()
     * @see jmri.util.FileUtil#getProgramPath()
     */
    static void initLog4J(@Nonnull String logFile) {
        Logger logger = LogManager.getLogger();
        Map<String, Appender> appenderMap = ((org.apache.logging.log4j.core.Logger) logger).getAppenders();
        if ( appenderMap.size() > 1 ) {
            log.debug("initLog4J already initialized!");
            return;
        }
        // Initialise JMRI System Console
        // Need to do this before initialising log4j so that the new
        // stdout and stderr streams are set up and usable by the ConsoleAppender
        if (!GraphicsEnvironment.isHeadless()) {
            SystemConsole.getInstance();
        }

        // initialize the java.util.logging to log4j bridge
        initializeJavaUtilLogging();

        // initialize log4j - from logging control file (lcf) only
        String loggingControlFileLocation = getLoggingConfig(logFile);
        if ( loggingControlFileLocation != null ) {
            configureLogging(loggingControlFileLocation);
        } else {
            Configurator.reconfigure();
            Configurator.setRootLevel(Level.INFO);
            log.error("Unable to load Configuration {}", logFile);
            if (!GraphicsEnvironment.isHeadless()) {
                JmriJOptionPane.showMessageDialog(null,
                    "Could not locate Logging Configuration file " + logFile,
                    "Could not Locate Logging Configuration File",
                    JmriJOptionPane.ERROR_MESSAGE);
            }
        }
        // install default exception handler so uncaught exceptions are logged, not printed
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    }

    @CheckForNull
    public static String getLoggingConfig(@Nonnull String logFileLocation) {
        if (new File(logFileLocation).isAbsolute() && new File(logFileLocation).canRead()) {
            return logFileLocation;
        } else if ( new File(FileUtil.getPreferencesPath() + logFileLocation).canRead()) {
            return FileUtil.getPreferencesPath() + logFileLocation;
        } else if ( new File(FileUtil.getProgramPath() + logFileLocation).canRead()) {
            return FileUtil.getProgramPath() + logFileLocation;
        } else {
            return null;
        }
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
        Logger logger = LogManager.getLogger();
        Map<String, Appender  > appenderMap = ((org.apache.logging.log4j.core.Logger) logger).getAppenders();
        appenderMap.forEach((key, a) -> {
            if (a instanceof RollingFileAppender) {
                RollingFileAppender rf = (RollingFileAppender)a;
                String fileName = rf.getFileName();
                if ( fileName.equals(rf.getFilePattern()) ) {
                    log.info("This log is stored in file: {}", fileName);
                } else {
                    log.info("This log is appended to file: {}", fileName);
                }
            } else if (a instanceof FileAppender) {
                log.info("This log is stored in file: {}", ((FileAppender) a).getFileName());
            }
        });
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
    static private void configureLogging(@Nonnull String configFile) {
        // System.out.println("Log4JUtil configureLogging " + configFile);

        // set the log4j config file location programatically
        // so that JUL adapter is enabled first
        // and Jython / JavaScript use the same LoggerContext
        System.setProperty("log4j2.configurationFile", configFile);

        // ensure the logging directory exists
        // if it's not writable, the console will get the error from log4j, so
        // we don't need to explictly test for that here, just make sure the
        // directory is created if need be.
        if (System.getProperty(SYS_PROP_LOG_PATH) == null ) {
            System.setProperty(SYS_PROP_LOG_PATH, FileUtil.getPreferencesPath() + "log" + File.separator);
        }
        File logDir = new File(System.getProperty(SYS_PROP_LOG_PATH));
        String createLogErr = null;
        if (!logDir.exists()) {
            try {
                Files.createDirectories(logDir.toPath());
            } catch ( IOException ex ) {
                createLogErr = "Could not create directory for log files, " + ex.getMessage();
            }
        }
        try {
            Configurator.initialize(null, configFile);
            log.debug("Logging initialised with {}", configFile);
        } catch ( Exception ex ) {
            Configurator.reconfigure();
            Configurator.setRootLevel(Level.INFO);
            if (!GraphicsEnvironment.isHeadless()) {
                JmriJOptionPane.showMessageDialog(null,
                        "Could not Initialise Logging " + ex.getMessage(),
                        configFile,
                        JmriJOptionPane.ERROR_MESSAGE);
            }
        }
        if (createLogErr!=null) { // wait until Logging init
            log.error("Could not create directory for log files at {} {}",
                System.getProperty(SYS_PROP_LOG_PATH), createLogErr);
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Log4JUtil.class);

}
