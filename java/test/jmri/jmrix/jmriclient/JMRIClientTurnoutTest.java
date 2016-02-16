package jmri.jmrix.jmriclient;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JMRIClientTurnoutTest.java
 *
 * Description:	tests for the jmri.jmrix.jmriclient.JMRIClientTurnout class
 *
 * @author	Bob Jacobsen
 * @version $Revision: 17977 $
 */
public class JMRIClientTurnoutTest extends TestCase {

    public void testCtor() {
        JMRIClientTrafficController tc = new JMRIClientTrafficController() {
            public void sendJMRIClientMessage(JMRIClientMessage m, JMRIClientListener reply) {
                // do nothing to avoid null pointer when sending to non-existant
                // connection durring test.
            }
        };
        JMRIClientTurnout m = new JMRIClientTurnout(3, new JMRIClientSystemConnectionMemo(tc));
        Assert.assertNotNull(m);
    }

    // from here down is testing infrastructure
    public JMRIClientTurnoutTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", JMRIClientTurnoutTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JMRIClientTurnoutTest.class);
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
