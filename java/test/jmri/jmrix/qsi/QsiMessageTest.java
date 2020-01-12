/**
 * QsiMessageTest.java
 *
 * Description:	JUnit tests for the QsiMessage class
 *
 * @author	Bob Jacobsen
 */
package jmri.jmrix.qsi;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class QsiMessageTest extends jmri.jmrix.AbstractMessageTestBase {

    private QsiSystemConnectionMemo memo = null;
    private QsiTrafficController tc = null;
    private QsiMessage msg = null;

    @Test
    public void testToASCIIString() {
        msg = new QsiMessage(5);
        msg.setOpCode(0x50);
        msg.setElement(1, 0x20);
        msg.setElement(2, 0x32);
        msg.setElement(3, 0x36);
        msg.setElement(4, 0x31);
        Assert.assertEquals("string compare ", "50 20 32 36 31 ", msg.toString());
    }

    @Test
    public void testGetEnable() {
        msg = QsiMessage.getEnableMain();
        Assert.assertEquals("length", 1, msg.getNumDataElements());
        Assert.assertEquals("opCode", 43, msg.getOpCode());
    }

    @Test
    public void testRecognizeEnable() {
        msg = QsiMessage.getEnableMain();
        Assert.assertEquals("isEnableMain", true, msg.isEnableMain());
        Assert.assertEquals("isKillMain", false, msg.isKillMain());
    }

    @Test
    public void testReadRegister() {
        msg = QsiMessage.getReadRegister(2);
        Assert.assertEquals("string compare ", "20 ", msg.toString());
    }

    @Test
    public void testWriteRegister() {
        msg = QsiMessage.getWriteRegister(2, 250);
        Assert.assertEquals("string compare ", "20 ", msg.toString());
    }

    // The minimal setup for log4J
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
        m = msg = new QsiMessage(1);
    }

    @After
    public void tearDown() {
	memo = null;
	tc = null;
	m = msg = null;
        JUnitUtil.tearDown();
    }

}
