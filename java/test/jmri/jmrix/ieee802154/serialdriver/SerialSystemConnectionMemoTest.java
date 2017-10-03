package jmri.jmrix.ieee802154.serialdriver;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * SerialSystemConnectionMemoTest.java
 *
 * Description:	tests for the
 * jmri.jmrix.ieee802154.serialdriver.SerialSystemConnectionMemo class
 *
 * @author	Paul Bender
 */
public class SerialSystemConnectionMemoTest extends TestCase {

    public void testCtor() {
        SerialSystemConnectionMemo m = new SerialSystemConnectionMemo();
        Assert.assertNotNull("exists", m);
    }

    // from here down is testing infrastructure
    public SerialSystemConnectionMemoTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SerialSystemConnectionMemoTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SerialSystemConnectionMemoTest.class);
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
