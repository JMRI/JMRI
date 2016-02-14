package jmri.jmrix.jmriclient;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JMRIClientSensorManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.jmriclient.JMRIClientSensorManager
 * class
 *
 * @author	Bob Jacobsen
 * @version $Revision: 17977 $
 */
public class JMRIClientSensorManagerTest extends TestCase {

    public void testCtor() {
        JMRIClientSensorManager m = new JMRIClientSensorManager(new JMRIClientSystemConnectionMemo());
        Assert.assertNotNull(m);
    }

    // from here down is testing infrastructure
    public JMRIClientSensorManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", JMRIClientSensorManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JMRIClientSensorManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(JMRIClientSensorManagerTest.class.getName());

}
