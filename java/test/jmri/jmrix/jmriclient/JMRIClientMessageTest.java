package jmri.jmrix.jmriclient;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * JMRIClientMessageTest.java
 *
 * Description:	tests for the jmri.jmrix.jmriclient.JMRIClientMessage class
 *
 * @author	Bob Jacobsen
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
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JMRIClientMessageTest.class);
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
