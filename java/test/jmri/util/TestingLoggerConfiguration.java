package jmri.util;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.util.exceptionhandler.UncaughtExceptionHandler;

import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.config.Configurator;

/**
 * Common utility methods for working with Log4J in tests.
 * <p>
 * See also jmri.apps.util.Log4JUtil
 * <p>
 * Used by JUnitUtil setUpLoggingAndCommonProperties
 * which passes either System Property 'jmri.log4jconfigfilename' , or if
 * unset 'tests_lcf.xml' as the Logging Configuration File Filename.
 *
 * @author Bob Jacobsen Copyright 2009, 2010
 * @author Randall Wood Copyright 2014, 2020
 */
public class TestingLoggerConfiguration {

    private static final String LOG_HEADER = "****** JMRI log *******";
    public static final String SYS_PROP_LOG_PATH =  "jmri.log.path";

    /**
     * Initialize logging, specifying a Configuration file.
     *
     * @param configFile the Logging Configuration file
     */
    static public void initLogging(@Nonnull String configFile) {
        // System.out.println("TestingLoggerConfiguration initLogging " + configFile);
        initLog4J(configFile);
    }

    /**
     * Initialize Log4J.
     * <p>
     * Use the logging control file specified in the <i>jmri.log</i> property
     * or, if none, the default_lcf.xml file. If the file is absolute and cannot be
     * found, look for the file first in the settings directory and then in the
     * installation directory.
     *
     * @param logConfigFile the logging control file
     * @see jmri.util.FileUtil#getPreferencesPath()
     * @see jmri.util.FileUtil#getProgramPath()
     */
    static void initLog4J(@Nonnull String logConfigFile) {
        Logger logger = LogManager.getLogger();
        Map<String, Appender> appenderMap = ((org.apache.logging.log4j.core.Logger) logger).getAppenders();
        if ( appenderMap.size() > 1 ) {
            log.debug("initLog4J already initialized!");
            return;
        }

        // System.out.println("about to init jul etc. ");
        // initialize the java.util.logging to log4j bridge
        initializeJavaUtilLogging();

        // initialize log4j - from logging control file (lcf) only
        String loggingControlFileLocation = getLoggingConfig(logConfigFile);
        if ( loggingControlFileLocation != null ) {
            configureLogging(loggingControlFileLocation);
        } else {
            Configurator.reconfigure();
            Configurator.setRootLevel(Level.WARN);
            System.err.println("Unable to load Configuration "+ logConfigFile);
        }
        // install default exception handler so uncaught exceptions are logged, not printed
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    }

    @CheckForNull
    public static String getLoggingConfig(@Nonnull String logConfigFileLocation) {
        if (new File(logConfigFileLocation).isAbsolute() && new File(logConfigFileLocation).canRead()) {
            return logConfigFileLocation;
        } else if ( new File(FileUtil.getPreferencesPath() + logConfigFileLocation).canRead()) {
            return FileUtil.getPreferencesPath() + logConfigFileLocation;
        } else if ( new File(FileUtil.getProgramPath() + logConfigFileLocation).canRead()) {
            return FileUtil.getProgramPath() + logConfigFileLocation;
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
                log.info("This log is appended to file: {}", ((RollingFileAppender) a).getFileName());
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
        // System.out.println("TestingLoggerConfiguration configureLogging " + configFile);

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
        if (!logDir.exists()) {
            try {
                Files.createDirectories(logDir.toPath());
            } catch ( IOException ex ) {
                System.err.println("Could not create directory for log files, " + ex.getMessage());
            }
        }

        try {
            Configurator.initialize(null, configFile);
            log.debug("Logging initialised with {}", configFile);
        } catch ( Exception ex ) {
            System.err.println("Could not initialise logging for log config file "
                + configFile + " " + ex);
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TestingLoggerConfiguration.class);
    
}
