
package apps.tests;

public class Log4JFixture extends java.lang.Object {

  public Log4JFixture(Object obj) {
    initLogging();
  }

  public void setUp() {
  }

  public void tearDown() {
  }

	static boolean log4jinit = true;
	public static void initLogging() {
		if (log4jinit) {
			log4jinit = false;
    		// initialize log4j - from logging control file (lcf) if you can find it
     		String logFile = "default.lcf";
    		if (new java.io.File(logFile).canRead()) {
    			System.out.println(logFile+" configures logging");
    			org.apache.log4j.PropertyConfigurator.configure("default.lcf");
   		} else {
    			System.out.println(logFile+" not found, using default logging");
	    		org.apache.log4j.BasicConfigurator.configure();
	   			// only log warnings and above
	   			org.apache.log4j.Category.getRoot().setPriority(org.apache.log4j.Priority.INFO);
     		}
		}
	}

  	static org.apache.log4j.Category log = null;

}
