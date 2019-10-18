package jmri.jmrix.nce;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the NceReplyclass
 *
 * @author	Bob Jacobsen
 */
public class NceReplyTest extends jmri.jmrix.AbstractMessageTestBase {
        
    private NceTrafficController tc = null;
    private NceReply msg = null;

    @Test
    public void testCreate() {
        msg.setElement(0, 'A');
        Assert.assertEquals("expected length ", 1, msg.getNumDataElements());
        msg.setElement(1, 'B');
        msg.setElement(2, 'C');
        Assert.assertEquals("expected length ", 3, msg.getNumDataElements());
        msg.setElement(0, '1');
        Assert.assertEquals("expected length ", 3, msg.getNumDataElements());
        msg.setElement(3, 'D');
        Assert.assertEquals("expected length ", 4, msg.getNumDataElements());
        msg.setElement(5, 'A');
        Assert.assertEquals("expected length ", 6, msg.getNumDataElements());

    }

    @Test
    public void testBinaryToString() {
        msg.setBinary(true);
        msg.setOpCode(0x81);
        msg.setElement(1, 0x02);
        msg.setElement(2, 0xA2);
        msg.setElement(3, 0x00);
        Assert.assertEquals("string compare ", "81 02 A2 00", msg.toString());
    }

    @Test
    public void testAsciiToString() {
        msg.setBinary(false);
        msg.setOpCode('C');
        msg.setElement(1, 'o');
        msg.setElement(2, 'm');
        msg.setElement(3, ':');
        Assert.assertEquals("string compare ", "Com:", msg.toString());
    }

    @Test
    public void testSkipWhiteSpace() {
        msg.setBinary(false);
        msg.setElement(0, '0');
        msg.setElement(1, ' ');
        msg.setElement(2, ' ');
        msg.setElement(3, 'A');
        msg.setElement(4, 0x0A);
        msg.setElement(5, 'A');
        Assert.assertEquals(" skip two blanks", 3, msg.skipWhiteSpace(1));
        Assert.assertEquals(" skip no blanks", 3, msg.skipWhiteSpace(3));
        Assert.assertEquals(" handle start", 0, msg.skipWhiteSpace(0));
        Assert.assertEquals(" skip LF", 5, msg.skipWhiteSpace(4));
    }

    @Test
    public void testSkipCOMMAND() {
        msg.setBinary(false);
        msg.setElement(0, ' ');
        msg.setElement(1, ' ');
        msg.setElement(2, 'A');
        msg.setElement(3, ' ');
        msg.setElement(4, 'C');
        msg.setElement(5, 'O');
        msg.setElement(6, 'M');
        msg.setElement(7, 'M');
        msg.setElement(8, 'A');
        msg.setElement(9, 'N');
        msg.setElement(10, 'D');
        msg.setElement(11, ':');
        msg.setElement(12, ' ');
        msg.setElement(13, 'X');
        Assert.assertEquals(" too short", 13, msg.skipPrefix(13));
        Assert.assertEquals(" not found", 5, msg.skipPrefix(5));
        Assert.assertEquals(" too short", 6, msg.skipPrefix(6));
        Assert.assertEquals(" too short", 7, msg.skipPrefix(7));
        Assert.assertEquals(" find & skip", 13, msg.skipPrefix(4));
        Assert.assertEquals(" not found", 0, msg.skipPrefix(0));
        msg = new NceReply(tc);
        msg.setBinary(false);
        msg.setElement(0, 'C');
        msg.setElement(1, 'O');
        msg.setElement(2, 'M');
        msg.setElement(3, 'M');
        msg.setElement(4, 'A');
        msg.setElement(5, 'N');
        msg.setElement(6, 'D');
        msg.setElement(7, ':');
        msg.setElement(8, ' ');
        msg.setElement(9, '0');
        msg.setElement(10, '2');
        msg.setElement(11, '7');
        Assert.assertEquals(" start of reply ", 9, msg.skipPrefix(0));
    }

    @Test
    public void testPollValue1() {
        msg.setBinary(true);
        msg.setElement(0, 0x02);
        msg.setElement(1, 0x00);
        msg.setElement(2, 0x01);
        msg.setElement(3, 0x02);
        Assert.assertEquals("value ", 0x0200, msg.pollValue());
    }

    @Test
    public void testPollValue2() {
        msg.setBinary(true);
        msg.setElement(0, 0x00);
        msg.setElement(1, 0x04);
        msg.setElement(2, 0x01);
        msg.setElement(3, 0x02);
        Assert.assertEquals("value ", 0x4, msg.pollValue());
    }

    @Test
    public void testPollValue3() {
        msg.setBinary(true);
        msg.setElement(0, 0x12);
        msg.setElement(1, 0x34);
        msg.setElement(2, 0x01);
        msg.setElement(3, 0x02);
        Assert.assertEquals("value ", 0x1234, msg.pollValue());
    }

    @Test
    public void testPollToString() {
        msg.setBinary(true);
        msg.setElement(0, 0x12);
        msg.setElement(1, 0x34);
        msg.setElement(2, 0x01);
        msg.setElement(3, 0x02);
        Assert.assertEquals("string value","12 34 01 02", msg.toString());
    }

    @Test
    public void testPollToMonitorString() {
        msg.setBinary(true);
        msg.setElement(0, 0x12);
        msg.setElement(1, 0x34);
        msg.setElement(2, 0x01);
        msg.setElement(3, 0x02);
        Assert.assertEquals("monitor string value","Reply: 12 34 01 02", msg.toMonitorString());
    }

    @Test
    public void testValue1() {
        // value when just the string comes back
        msg.setBinary(false);
        msg.setElement(0, '0');
        msg.setElement(1, '2');
        msg.setElement(2, '7');
        msg.setElement(3, ' ');
        Assert.assertEquals("value ", 27, msg.value());
    }

    @Test
    public void testValue2() {
        // value with a "Command:" prefix
        msg.setBinary(false);
        msg.setElement(0, 'C');
        msg.setElement(1, 'O');
        msg.setElement(2, 'M');
        msg.setElement(3, 'M');
        msg.setElement(4, 'A');
        msg.setElement(5, 'N');
        msg.setElement(6, 'D');
        msg.setElement(7, ':');
        msg.setElement(8, ' ');
        msg.setElement(9, '0');
        msg.setElement(10, '2');
        msg.setElement(11, '7');
        Assert.assertEquals("value ", 27, msg.value());
    }

    @Test
    public void testMatch() {
        msg = new NceReply(tc, "**** PROGRAMMING MODE - MAIN TRACK NOW DISCONNECTED ****");
        Assert.assertEquals("find ", 5, msg.match("PROGRAMMING"));
        Assert.assertEquals("not find ", -1, msg.match("foo"));
    }

    @Override
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        tc = new NceTrafficController();
        m = msg = new NceReply(tc);
    }

    @After
    public void tearDown() {
	    m = msg = null;
	    tc = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        jmri.util.JUnitUtil.tearDown();
    }

}
