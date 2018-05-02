package jmri.jmrix.openlcb;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * OlcbProgrammerTest.java
 *
 * Description:	tests for the jmri.jmrix.openlcb.OlcbProgrammer class
 *
 * @author	Bob Jacobsen
 */
public class OlcbProgrammerTest extends TestCase {

    public void testCtor() {
        new OlcbSystemConnectionMemo();
        OlcbProgrammer s = new OlcbProgrammer();
        Assert.assertNotNull(s);
    }

    // from here down is testing infrastructure
    public OlcbProgrammerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", OlcbProgrammerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(OlcbProgrammerTest.class);
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
