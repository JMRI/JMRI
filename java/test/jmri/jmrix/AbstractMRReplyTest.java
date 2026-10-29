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

    AbstractMRReply testMsg = null;

    @Test
    public void testSimpleMatch1() {
        Assertions.assertNotNull(testMsg);
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

    @Test
    public void testReceivedTime() {
        Assertions.assertFalse(testMsg.hasReceivedTime(), "not recorded");
        long before = System.nanoTime();
        long t = testMsg.getReceivedTime();
        Assertions.assertTrue(t >= before && t <= System.nanoTime(), "not recorded: now");

        testMsg.setReceivedTime(1234L);
        Assertions.assertTrue(testMsg.hasReceivedTime(), "recorded");
        Assertions.assertEquals(1234L, testMsg.getReceivedTime());

        AbstractMRReply copy = new AbstractMRReply(testMsg) {
            @Override
            protected int skipPrefix(int index) {
                return 0;
            }
        };
        Assertions.assertTrue(copy.hasReceivedTime(), "copied");
        Assertions.assertEquals(1234L, copy.getReceivedTime(), "copied");
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
    @Override
    public void tearDown() {
        m = testMsg = null;
        JUnitUtil.tearDown();
    }

}
