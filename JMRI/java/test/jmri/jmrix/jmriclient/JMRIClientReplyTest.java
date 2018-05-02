package jmri.jmrix.jmriclient;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * JMRIClientReplyTest.java
 *
 * Description:	tests for the jmri.jmrix.jmriclient.JMRIClientReply class
 *
 * @author	Bob Jacobsen
 */
public class JMRIClientReplyTest extends TestCase {

    public void testCtor() {
        JMRIClientReply m = new JMRIClientReply();
        Assert.assertNotNull(m);
    }

    // from here down is testing infrastructure
    public JMRIClientReplyTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", JMRIClientReplyTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JMRIClientReplyTest.class);
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
