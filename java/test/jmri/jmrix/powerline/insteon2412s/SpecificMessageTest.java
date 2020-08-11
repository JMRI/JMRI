package jmri.jmrix.powerline.insteon2412s;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JUnit tests for the cm11.SpecficMessage class.
 *
 * @author Bob Jacobsen Copyright 2003, 2007, 2008, 2009
 */
public class SpecificMessageTest extends jmri.jmrix.AbstractMessageTestBase {

    private SpecificMessage msg = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = msg = new SpecificMessage(4);
    }

    @AfterEach
    public void tearDown() {
        m = msg = null;
        JUnitUtil.tearDown();
    }

    @Test
    public void testBytesToString() {
        msg = new SpecificMessage(4);
        msg.setOpCode(0x81);
        msg.setElement(1, (byte) 0x02);
        msg.setElement(2, (byte) 0xA2);
        msg.setElement(3, (byte) 0x00);
        Assert.assertEquals("string compare ", "81 02 A2 00", msg.toString());
    }

}
