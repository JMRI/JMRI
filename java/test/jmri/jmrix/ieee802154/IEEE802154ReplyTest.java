package jmri.jmrix.ieee802154;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * IEEE802154ReplyTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.IEEE802154Reply class
 *
 * @author	Paul Bender
 */
public class IEEE802154ReplyTest extends TestCase {

    public void testCtor() {
        IEEE802154TrafficController tc = new IEEE802154TrafficController(){
           public IEEE802154Node newNode(){
             return null;
           }

           public jmri.jmrix.AbstractMRReply newReply(){
             return null;
           }
        };
        IEEE802154Reply m = new IEEE802154Reply(tc);
        Assert.assertNotNull(m);
        
        jmri.util.JUnitAppender.assertErrorMessage("Deprecated Method setInstance called");
    }

    // from here down is testing infrastructure
    public IEEE802154ReplyTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", IEEE802154ReplyTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(IEEE802154ReplyTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
