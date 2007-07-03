// AllTest.java

package apps.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invoke all the JMRI project JUnit tests via a GUI interface.
 *
 * @author	Bob Jacobsen
 * @version	$Revision: 1.7 $
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
		String[] testCaseName = {"-noloading", AllTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite
    public static Test suite() {
        // all tests from here down in heirarchy
        TestSuite suite = new TestSuite("AllTest");  // no tests in this class itself
        // all tests from other classes
        suite.addTest(jmri.JmriTest.suite());

        return suite;
    }

    public static void initLogging(){
        apps.tests.Log4JFixture.initLogging();
    }
    
    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
