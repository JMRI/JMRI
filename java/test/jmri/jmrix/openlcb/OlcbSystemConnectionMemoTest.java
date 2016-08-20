package jmri.jmrix.openlcb;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * OlcbSystemConnectionMemoTest.java
 *
 * Description:	tests for the jmri.jmrix.openlcb.OlcbSystemConnectionMemo class
 *
 * @author	Bob Jacobsen
 * @version $Revision: 28820 $
 */
public class OlcbSystemConnectionMemoTest extends TestCase {

    public void testCtor() {
    }

    // from here down is testing infrastructure
    public OlcbSystemConnectionMemoTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", OlcbSystemConnectionMemoTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(OlcbSystemConnectionMemoTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
