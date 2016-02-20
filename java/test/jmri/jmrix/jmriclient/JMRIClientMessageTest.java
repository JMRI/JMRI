package jmri.jmrix.jmriclient;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JMRIClientMessageTest.java
 *
 * Description:	tests for the jmri.jmrix.jmriclient.JMRIClientMessage class
 *
 * @author	Bob Jacobsen
 * @version $Revision: 17977 $
 */
public class JMRIClientMessageTest extends TestCase {

    public void testCtor() {
        JMRIClientMessage m = new JMRIClientMessage(3);
        Assert.assertEquals("length", 3, m.getNumDataElements());
    }

    // from here down is testing infrastructure
    public JMRIClientMessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", JMRIClientMessageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JMRIClientMessageTest.class);
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
