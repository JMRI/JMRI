package jmri.jmrix;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for AbstractMRReply.
 *
 * @author Bob Jacobsen
 */
public class AbstractMRReplyTest extends AbstractMessageTestBase {

    AbstractMRReply testMsg;

    @Test
    public void testSimpleMatch1() {
        Assert.assertEquals("match", 0, testMsg.match("foo"));
    }

    @Test
    public void testSimpleMatch2() {
        testMsg = new AbstractMRReply("foo1") {
            @Override
            protected int skipPrefix(int index) {
                return 0;
            }
        };

        Assert.assertEquals("match", 0, testMsg.match("foo"));
    }

    @Test
    public void testSimpleMatch3() {
        testMsg = new AbstractMRReply("ffffffff") {
            @Override
            protected int skipPrefix(int index) {
                return 0;
            }
        };

        Assert.assertEquals("match", 0, testMsg.match("f"));
    }

    @Test
    public void testDelaySimpleMatch1() {
        testMsg = new AbstractMRReply("123 foo") {
            @Override
            protected int skipPrefix(int index) {
                return 0;
            }
        };

        Assert.assertEquals("match", 4, testMsg.match("foo"));
    }

    @Test
    public void testDelaySimpleMatch2() {
        testMsg = new AbstractMRReply("123 foo 123") {
            @Override
            protected int skipPrefix(int index) {
                return 0;
            }
        };

        Assert.assertEquals("match", 4, testMsg.match("foo"));
    }

    @Test
    public void testOverlapMatch() {
        testMsg = new AbstractMRReply("1fo foo 123") {
            @Override
            protected int skipPrefix(int index) {
                return 0;
            }
        };

        Assert.assertEquals("match", 4, testMsg.match("foo"));
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        m = testMsg = new AbstractMRReply("foo") {
            @Override
            protected int skipPrefix(int index) {
                return 0;
            }
        };
    }

    @AfterEach
    public void tearDown() {
        m = testMsg = null;
        JUnitUtil.tearDown();
    }

}
