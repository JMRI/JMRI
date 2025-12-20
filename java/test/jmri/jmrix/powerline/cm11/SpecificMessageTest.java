package jmri.jmrix.powerline.cm11;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.jmrix.powerline.SerialMessage;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the cm11.SpecficMessage class.
 *
 * @author Bob Jacobsen Copyright 2003, 2007, 2008
 */
public class SpecificMessageTest {

    @Test
    public void testCreate() {
        SerialMessage m = new SpecificMessage(4);
        assertNotNull( m, "exists");
    }

    @Test
    public void testBytesToString() {
        SerialMessage m = new SpecificMessage(4);
        m.setOpCode(0x81);
        m.setElement(1, (byte) 0x02);
        m.setElement(2, (byte) 0xA2);
        m.setElement(3, (byte) 0x00);
        assertEquals( "81 02 A2 00", m.toString(), "string compare ");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
