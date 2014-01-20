// Log4JUtil.java
package jmri.util;

import apps.SystemConsole;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import jmri.util.exceptionhandler.AwtHandler;
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
 * <P>
 * Two system properties influence how logging is configured in JMRI:
 * <dl>
 * <dt>jmri.log</dt><dd>The logging control file. If this file is not an
 * absolute path, this file is searched for in the following order:<ol>
 * <li>Current working directory</li>
 * <li>JMRI settings directory</li>
 * <li>JMRI installation (program) directory</li>
 * </ol>
 * If this property is not specified, the logging control file
 * <i>default.lcf</i> is used, following the above search order to find it.
 * </dd>
 * <dt>jmri.log.path</dt><dd>The directory for storing logs. If not specified,
 * logs are stored in the JMRI settings directory.</dd>
 * </dl>
 *
 * @author Bob Jacobsen Copyright 2009, 2010
 * @author Randall Wood Copyright 2014
 * @version $Revision$
 */
public class Log4JUtil {

    private static boolean log4JSetUp = false;
    private static final String jmriLog = "****** JMRI log *******";
    private static final Logger log = LoggerFactory.getLogger(Log4JUtil.class.getName());

    /**
     * Initialize Log4J.
     * <p>
     * Use the logging control file <i>default.lcf</i> or the file specified in
     * the <i>jmri.log</i> property. If the file cannot be found in the current
     * directory, look for the file first in the settings directory and then in
     * the installation directory.
     *
     * @see jmri.util.FileUtil#getPreferencesPath()
     * @see jmri.util.FileUtil#getProgramPath()
     */
    static public void initLog4J() {
        if (log4JSetUp) {
            log.debug("initLog4J already initialized!");
            return;
        }
        // Initialise JMRI System Console
        // Need to do this before initialising log4j so that the new
        // stdout and stderr streams are set-up and usable by the ConsoleAppender
        SystemConsole.create();
        log4JSetUp = true;
        // initialize log4j - from logging control file (lcf) only
        String logFile = System.getProperty("jmri.log", "default.lcf");
        try {
            if (new File(logFile).canRead()) {
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
        // install default exception handlers
        System.setProperty("sun.awt.exception.handler", AwtHandler.class.getName());
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    }

    @SuppressWarnings("unchecked")
    static public String startupInfo(String program) {
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
                + " at " + (new java.util.Date()));
    }

    /**
     * Configure Log4J using the specified properties file.
     * <p>
     * This method sets the system property <i>jmri.log.path</i> to the JMRI
     * settings directory if not specified.
     *
     * @param config
     * @throws IOException
     * @see jmri.util.FileUtil#getPreferencesPath()
     */
    static private void configureLogging(String config) throws IOException {
        Properties p = new Properties();
        p.load(new FileInputStream(config));
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
            logDir.mkdirs();
        }
        PropertyConfigurator.configure(p);
    }

}
