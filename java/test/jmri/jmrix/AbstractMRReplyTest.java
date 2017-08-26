package jmri.jmrix;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for AbstractMRReply
 *
 * @author	Bob Jacobsen
  */
public class AbstractMRReplyTest extends TestCase {

    AbstractMRReply testMsg;

    public AbstractMRReplyTest(String s) {
        super(s);
    }

    public void testSimpleMatch1() {
        testMsg = new AbstractMRReply("foo") {
            @Override
            protected int skipPrefix(int index) {
                return 0;
            }
        };

        Assert.assertEquals("match", 0, testMsg.match("foo"));
    }

    public void testSimpleMatch2() {
        testMsg = new AbstractMRReply("foo1") {
            @Override
            protected int skipPrefix(int index) {
                return 0;
            }
        };

        Assert.assertEquals("match", 0, testMsg.match("foo"));
    }

    public void testSimpleMatch3() {
        testMsg = new AbstractMRReply("ffffffff") {
            @Override
            protected int skipPrefix(int index) {
                return 0;
            }
        };

        Assert.assertEquals("match", 0, testMsg.match("f"));
    }

    public void testDelaySimpleMatch1() {
        testMsg = new AbstractMRReply("123 foo") {
            @Override
            protected int skipPrefix(int index) {
                return 0;
            }
        };

        Assert.assertEquals("match", 4, testMsg.match("foo"));
    }

    public void testDelaySimpleMatch2() {
        testMsg = new AbstractMRReply("123 foo 123") {
            @Override
            protected int skipPrefix(int index) {
                return 0;
            }
        };

        Assert.assertEquals("match", 4, testMsg.match("foo"));
    }

    public void testOverlapMatch() {
        testMsg = new AbstractMRReply("1fo foo 123") {
            @Override
            protected int skipPrefix(int index) {
                return 0;
            }
        };

        Assert.assertEquals("match", 4, testMsg.match("foo"));
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {AbstractMRReplyTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(AbstractMRReplyTest.class);
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
