// Log4JUtil.java

package jmri.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Enumeration;

/**
 * Common utility methods for working with Log4J.
 * <P>
 * We needed a place to refactor common Log4J idioms in JMRI
 * code, so this class was created. It's more of a library of procedures
 * than a real class, as (so far) all of the operations have needed no state
 * information.
 *
 * @author Bob Jacobsen  Copyright 2009, 2010
 * @version $Revision$
 */

public class Log4JUtil {

    static private boolean log4JSetUp = false;
    
    /**
     * Our standard initialization of 
     * Log4J
     */
    static public void initLog4J() {
        if (log4JSetUp) return;
        log4JSetUp = true;
        // initialize log4j - from logging control file (lcf) only
        // if can find it!
        String logFile = "default.lcf";
        try {
            if (new java.io.File(logFile).canRead()) {
                org.apache.log4j.PropertyConfigurator.configure(logFile);
            } else {
                org.apache.log4j.BasicConfigurator.configure();
                org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.WARN);
            }
        }
        catch (java.lang.NoSuchMethodError e) { log.error("Exception starting logging: "+e); }
        // install default exception handlers
        System.setProperty("sun.awt.exception.handler", jmri.util.exceptionhandler.AwtHandler.class.getName());
        Thread.setDefaultUncaughtExceptionHandler(new jmri.util.exceptionhandler.UncaughtExceptionHandler());
    }

    @SuppressWarnings("unchecked")
	static public String startupInfo(String program) {
    	log.info(jmriLog);
        Enumeration<org.apache.log4j.Logger> e = org.apache.log4j.Logger.getRootLogger().getAllAppenders();
        while ( e.hasMoreElements() ) {
            org.apache.log4j.Appender a = (org.apache.log4j.Appender)e.nextElement();
            if ( a instanceof org.apache.log4j.RollingFileAppender ) {
                log.info("This log is stored in file: "+((org.apache.log4j.RollingFileAppender)a).getFile());
            }
            else if ( a instanceof org.apache.log4j.FileAppender ) {
                log.info("This log is stored in file: "+((org.apache.log4j.FileAppender)a).getFile());
            }
        }
        return (program+" version "+jmri.Version.name()
                +" starts under Java "+System.getProperty("java.version","<unknown>")
                +" at "+(new java.util.Date()));
    }

    private static final String jmriLog ="****** JMRI log *******";

    static Logger log = LoggerFactory.getLogger(Log4JUtil.class.getName());

}
