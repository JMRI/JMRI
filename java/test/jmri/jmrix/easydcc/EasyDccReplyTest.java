/**
 * EasyDccReplyTest.java
 *
 * Description:	JUnit tests for the EasyDccReplyclass
 *
 * @author	Bob Jacobsen
 */
package jmri.jmrix.easydcc;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EasyDccReplyTest extends jmri.jmrix.AbstractMessageTestBase {

    private EasyDccReply msg = null;

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
        EasyDccReply msg = new EasyDccReply();
        msg.setOpCode('C');
        msg.setElement(1, 'o');
        msg.setElement(2, 'm');
        msg.setElement(3, ':');
        Assert.assertEquals("string compare ", "Com:", msg.toString());
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
    public void testValue1() {
        // value when just the string comes back
        int i = 0;
        msg.setElement(i++, 'C');
        msg.setElement(i++, 'V');
        msg.setElement(i++, '0');
        msg.setElement(i++, '0');
        msg.setElement(i++, '1');
        msg.setElement(i++, '2');
        msg.setElement(i++, '7');
        Assert.assertEquals("value ", 39, msg.value());
    }

    @Test
    public void testValue2() {
        // value when a hex string comes back
        int i = 0;
        msg.setElement(i++, 'C');
        msg.setElement(i++, 'V');
        msg.setElement(i++, '0');
        msg.setElement(i++, '0');
        msg.setElement(i++, '1');
        msg.setElement(i++, 'A');
        msg.setElement(i++, 'B');
        Assert.assertEquals("value ", 10 * 16 + 11, msg.value());
    }

    @Test
    public void testMatch() {
        msg = new EasyDccReply("**** PROGRAMMING MODE - MAIN TRACK NOW DISCONNECTED ****");
        Assert.assertEquals("find ", 5, msg.match("PROGRAMMING"));
        Assert.assertEquals("not find ", -1, msg.match("foo"));
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = msg = new EasyDccReply();
    }

    @After
    public void tearDown() {
	m = msg = null;
        JUnitUtil.tearDown();
    }

}
