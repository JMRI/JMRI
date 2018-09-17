package jmri.jmrix.grapevine;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * JUnit tests for the SerialMessage class.
 *
 * @author	Bob Jacobsen Copyright 2003, 2007, 2008
 */
public class SerialMessageTest extends jmri.jmrix.AbstractMessageTestBase {

    private SerialMessage msg = null;

    @Before
    @Override
    public void setUp() {
	JUnitUtil.setUp();
        m = msg = new SerialMessage();
    }

    @After
    public void tearDown(){
        m = msg = null;
	JUnitUtil.tearDown();
    }

    public void testBytesToString() {
        msg.setOpCode(0x81);
        msg.setElement(1, (byte) 0x02);
        msg.setElement(2, (byte) 0xA2);
        msg.setElement(3, (byte) 0x00);
        Assert.assertEquals("string compare ", "81 02 A2 00", msg.toString());
    }

    public void testSetParity1() {
        msg.setElement(0, (byte) 129);
        msg.setElement(1, (byte) 90);
        msg.setElement(2, (byte) 129);
        msg.setElement(3, (byte) (31 & 0xF0));
        msg.setParity();
        Assert.assertEquals("string compare ", "81 5A 81 1F", msg.toString());
    }

    public void testSetParity2() {
        msg.setElement(0, (byte) 226);
        msg.setElement(1, (byte) 13);
        msg.setElement(2, (byte) 226);
        msg.setElement(3, (byte) 88);
        msg.setParity();
        Assert.assertEquals("string compare ", "E2 0D E2 58", msg.toString());
    }

    public void testSetParity3() {
        msg.setElement(0, (byte) 226);
        msg.setElement(1, (byte) 14);
        msg.setElement(2, (byte) 226);
        msg.setElement(3, (byte) 86);
        msg.setParity();
        Assert.assertEquals("string compare ", "E2 0E E2 56", msg.toString());
    }

    public void testSetParity4() {
        msg.setElement(0, (byte) 226);
        msg.setElement(1, (byte) 15);
        msg.setElement(2, (byte) 226);
        msg.setElement(3, (byte) 84);
        msg.setParity();
        Assert.assertEquals("string compare ", "E2 0F E2 54", msg.toString());
    }

    public void testSetParity5() {
        // observed error message
        msg.setElement(0, (byte) 0x80);
        msg.setElement(1, (byte) 98);
        msg.setElement(2, (byte) 0x80);
        msg.setElement(3, (byte) 0x10);
        msg.setParity();
        Assert.assertEquals("string compare ", "80 62 80 10", msg.toString());
    }

    public void testSetParity6() {
        // special req software version
        msg.setElement(0, (byte) 0xE2);
        msg.setElement(1, (byte) 119);
        msg.setElement(2, (byte) 0xE2);
        msg.setElement(3, (byte) 119);
        msg.setParity();
        Assert.assertEquals("string compare ", "E2 77 E2 77", msg.toString());
    }

    public void testSetParity7() {
        // from doc page
        msg.setElement(0, (byte) 129);
        msg.setElement(1, (byte) 90);
        msg.setElement(2, (byte) 129);
        msg.setElement(3, (byte) 31);
        msg.setParity();
        Assert.assertEquals("string compare ", "81 5A 81 1F", msg.toString());
    }

}
