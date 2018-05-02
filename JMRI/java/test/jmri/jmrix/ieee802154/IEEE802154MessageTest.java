package jmri.jmrix.ieee802154;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * IEEE802154MessageTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.IEEE802154Message class
 *
 * @author	Paul Bender
 */
public class IEEE802154MessageTest extends TestCase {

    public void testCtor() {
        IEEE802154Message m = new IEEE802154Message(3);
        Assert.assertEquals("length", 3, m.getNumDataElements());
    }

    // from here down is testing infrastructure
    public IEEE802154MessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", IEEE802154MessageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(IEEE802154MessageTest.class);
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
