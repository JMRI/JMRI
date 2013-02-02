
package apps.tests;

import org.apache.log4j.Logger;

public class Log4JFixture extends java.lang.Object {

  public Log4JFixture() {
    initLogging();
  }

  static public void setUp() {
    // always init logging if needed
    initLogging();
    //
    jmri.util.JUnitAppender.start();
  }

  static public void tearDown() {
    jmri.util.JUnitAppender.end();
  }

	static boolean log4jinit = true;
	public static void initLogging() {
		if (log4jinit) {
			log4jinit = false;
    		// initialize log4j - from logging control file (lcf) if you can find it
     		String logFile = "tests.lcf";
    		if (new java.io.File(logFile).canRead()) {
    			System.out.println(logFile+" configures logging");
    			org.apache.log4j.PropertyConfigurator.configure(logFile);
            } else {
                System.out.println(logFile+" not found, using default logging");
                
                // create an appender, and load it with a default pattern
                jmri.util.JUnitAppender a = new jmri.util.JUnitAppender();
                a.setLayout(new org.apache.log4j.PatternLayout(org.apache.log4j.PatternLayout.TTCC_CONVERSION_PATTERN));
                a.activateOptions();
                
                // configure Log4J using that appender
                org.apache.log4j.BasicConfigurator.configure(a);
                
                // only log warnings and above
                org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.WARN);
            }
            // install default exception handlers
            System.setProperty("sun.awt.exception.handler", jmri.util.exceptionhandler.AwtHandler.class.getName());
            Thread.setDefaultUncaughtExceptionHandler(new jmri.util.exceptionhandler.UncaughtExceptionHandler());
		}
	}

  	static Logger log = null;
}
