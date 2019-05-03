/**
 * EasyDccMessageTest.java
 *
 * Description:	JUnit tests for the EasyDccMessage class
 *
 * @author	Bob Jacobsen
 */
package jmri.jmrix.easydcc;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EasyDccMessageTest extends jmri.jmrix.AbstractMessageTestBase {
        
    private EasyDccMessage msg = null;

    @Test
    public void testToASCIIString() {
        msg = new EasyDccMessage(5);
        msg.setOpCode(0x50);
        msg.setElement(1, 0x20);
        msg.setElement(2, 0x32);
        msg.setElement(3, 0x36);
        msg.setElement(4, 0x31);
        Assert.assertEquals("string compare ", "P 261", msg.toString());
    }

    @Test
    public void testGetEnable() {
        msg = EasyDccMessage.getEnableMain();
        Assert.assertEquals("length", 1, msg.getNumDataElements());
        Assert.assertEquals("opCode", 'E', msg.getOpCode());
    }

    @Test
    public void testRecognizeEnable() {
        msg = EasyDccMessage.getEnableMain();
        Assert.assertEquals("isEnableMain", true, msg.isEnableMain());
        Assert.assertEquals("isKillMain", false, msg.isKillMain());
    }

    @Test
    public void testReadPagedCV() {
        msg = EasyDccMessage.getReadPagedCV(12);
        Assert.assertEquals("string compare ", "R 00C", msg.toString());
    }

    @Test
    public void testWritePagedCV() {
        msg = EasyDccMessage.getWritePagedCV(12, 126);
        Assert.assertEquals("string compare ", "P 00C 7E", msg.toString());
    }

    @Test
    public void testReadRegister() {
        msg = EasyDccMessage.getReadRegister(2);
        Assert.assertEquals("string compare ", "V2", msg.toString());
    }

    @Test
    public void testWriteRegister() {
        msg = EasyDccMessage.getWriteRegister(2, 250);
        Assert.assertEquals("string compare ", "S2 FA", msg.toString());
    }

    @Override
    @Before
    public void setUp() {
	JUnitUtil.setUp();
	m = msg = new EasyDccMessage(1);
    }

    @After
    public void tearDown(){
	m = msg = null;
	JUnitUtil.tearDown();
    }

}
