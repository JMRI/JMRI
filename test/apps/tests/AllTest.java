// AllTest.java

package apps.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invoke all the JMRI project JUnit tests via a GUI interface.
 *
 * @author	Bob Jacobsen
 * @version	$Revision: 1.6 $
 */
public class AllTest extends TestCase  {
    public AllTest(String s) {
        super(s);
    }

    // note that initLogging has to be invoked _twice_ to get logging to
    // work in both the test routines themselves, and also in the TestSuite
    // code.  And even though it should be protected by a static, it runs
    // twice!  There are probably two sets of classes being created here...

    // Main entry point
    static public void main(String[] args) {
        System.out.println("AllTest starts");
        String[] testCaseName = {AllTest.class.getName()};
        log = org.apache.log4j.Category.getInstance("jmri");
        // initialize junit
		junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite
    public static Test suite() {
        initLogging();
        // all tests from here down in heirarchy
        TestSuite suite = new TestSuite("AllTest");  // no tests in this class itself
        // all tests from other classes
        suite.addTest(jmri.JmriTest.suite());

        return suite;
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
                org.apache.log4j.Category.getRoot().setPriority(org.apache.log4j.Priority.ERROR);
            }
        }
    }

    static org.apache.log4j.Category log = null;

}
