package jmri.jmrix.ieee802154;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IEEE802154MessageTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.IEEE802154Message class
 *
 * @author	Paul Bender
 * @version $Revision$
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
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(IEEE802154MessageTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(IEEE802154MessageTest.class.getName());

}
