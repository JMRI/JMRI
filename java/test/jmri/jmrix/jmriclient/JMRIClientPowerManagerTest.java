package jmri.jmrix.jmriclient;

import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JMRIClientPowerManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.jmriclient.JMRIClientPowerManager class
 *
 * @author	Bob Jacobsen
 */
public class JMRIClientPowerManagerTest extends TestCase {

    public void testCtor() {
        JMRIClientPowerManager m = new JMRIClientPowerManager(new JMRIClientSystemConnectionMemo());
        Assert.assertNotNull(m);
    }

    // from here down is testing infrastructure
    public JMRIClientPowerManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", JMRIClientPowerManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JMRIClientPowerManagerTest.class);
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
