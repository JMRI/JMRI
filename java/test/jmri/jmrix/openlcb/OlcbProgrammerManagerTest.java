package jmri.jmrix.openlcb;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * OlcbProgrammerManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.openlcb.OlcbProgrammerManager class
 *
 * @author	Bob Jacobsen
 */
public class OlcbProgrammerManagerTest extends TestCase {

    public void testCtor() {
        new OlcbSystemConnectionMemo();
        OlcbProgrammerManager s = new OlcbProgrammerManager(new OlcbProgrammer());
        Assert.assertNotNull(s);
    }

    // from here down is testing infrastructure
    public OlcbProgrammerManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", OlcbProgrammerManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(OlcbProgrammerManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }
}
