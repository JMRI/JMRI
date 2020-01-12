package jmri.jmrix.qsi;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the QsiReplyclass
 *
 * @author	Bob Jacobsen Copyright 2006, 2007
 *
 */
public class QsiReplyTest extends jmri.jmrix.AbstractMessageTestBase {

    private QsiSystemConnectionMemo memo = null;
    private QsiTrafficController tc = null;
    private QsiReply msg = null;

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
    public void testAsciiToString() {
        msg.setOpCode('C');
        msg.setElement(1, 'o');
        msg.setElement(2, 'm');
        msg.setElement(3, ':');
        Assert.assertEquals("string compare ", "43 6F 6D 3A ", msg.toString(tc));
    }

    @Test
    public void testSkipWhiteSpace() {
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
    public void testMatch() {
        msg = new QsiReply("**** PROGRAMMING MODE - MAIN TRACK NOW DISCONNECTED ****");
        Assert.assertEquals("find ", 5, msg.match("PROGRAMMING"));
        Assert.assertEquals("not find ", -1, msg.match("foo"));
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new QsiSystemConnectionMemo();
        tc = new QsiTrafficControlScaffold(){
            @Override
            public boolean isSIIBootMode(){
                return true;
            }
        };
        memo.setQsiTrafficController(tc);
        m = msg = new QsiReply();
    }

    @After
    public void tearDown() {
	memo = null;
	tc = null;
	m = msg = null;
        JUnitUtil.tearDown();
    }

}
