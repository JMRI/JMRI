package jmri.jmrix.easydcc.easydccmon;

import jmri.jmrix.easydcc.EasyDccSystemConnectionMemo;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.*;

/**
 * JUnit tests for the EasyDccProgrammer class
 *
 * @author	Bob Jacobsen
 */
public class EasyDccMonActionTest extends TestCase {

    public void testCreate() {
        EasyDccMonAction a = new EasyDccMonAction("Monitor", new EasyDccSystemConnectionMemo("E", "EasyDCC via Serial"));
        Assert.assertNotNull("exists", a);
    }

    public EasyDccMonActionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {EasyDccMonActionTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EasyDccMonActionTest.class);
        return suite;
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}
