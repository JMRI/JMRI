package jmri.jmrix.jmriclient;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * JMRIClientReporterTest.java
 *
 * Description:	tests for the jmri.jmrix.jmriclient.JMRIClientReporter class
 *
 * @author	Bob Jacobsen
 */
public class JMRIClientReporterTest extends TestCase {

    public void testCtor() {
        JMRIClientTrafficController tc = new JMRIClientTrafficController() {
            @Override
            public void sendJMRIClientMessage(JMRIClientMessage m, JMRIClientListener reply) {
                // do nothing to avoid null pointer when sending to non-existant
                // connection durring test.
            }
        };
        JMRIClientReporter m = new JMRIClientReporter(3, new JMRIClientSystemConnectionMemo(tc));
        Assert.assertNotNull(m);
    }

    // from here down is testing infrastructure
    public JMRIClientReporterTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", JMRIClientReporterTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JMRIClientReporterTest.class);
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
