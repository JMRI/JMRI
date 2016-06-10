package jmri.jmrix.jmriclient;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JMRIClientReporterManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.jmriclient.JMRIClientReporterManager
 * class
 *
 * @author	Bob Jacobsen
 * @version $Revision: 17977 $
 */
public class JMRIClientReporterManagerTest extends TestCase {

    public void testCtor() {
        JMRIClientReporterManager m = new JMRIClientReporterManager(new JMRIClientSystemConnectionMemo());
        Assert.assertNotNull(m);
    }

    // from here down is testing infrastructure
    public JMRIClientReporterManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", JMRIClientReporterManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JMRIClientReporterManagerTest.class);
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
