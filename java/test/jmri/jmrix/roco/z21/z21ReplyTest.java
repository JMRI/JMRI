package jmri.jmrix.roco.z21;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * z21ReplyTest.java
 *
 * Description:	tests for the jmri.jmrix.roco.z21.z21Reply class
 *
 * @author	Bob Jacobsen
 * @version $Revision$
 */
public class z21ReplyTest extends TestCase {

    public void testCtor() {
        z21Reply m = new z21Reply();
        Assert.assertNotNull(m);
    }

    // test the string constructor.
    public void testStringCtor() {
        z21Message m = new z21Message("0D 00 04 00 12 34 AB 3 19 6 B B1");
        Assert.assertEquals("length", 12, m.getNumDataElements());
        Assert.assertEquals("0th byte", 0x0D, m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x00, m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0x04, m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", 0x00, m.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", 0x12, m.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", 0x34, m.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", 0xAB, m.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", 0x03, m.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", 0x19, m.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", 0x06, m.getElement(9) & 0xFF);
        Assert.assertEquals("10th byte", 0x0B, m.getElement(10) & 0xFF);
        Assert.assertEquals("11th byte", 0xB1, m.getElement(11) & 0xFF);
    }

    // from here down is testing infrastructure
    public z21ReplyTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", z21ReplyTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(z21ReplyTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(z21ReplyTest.class.getName());

}
