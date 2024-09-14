package jmri.jmrix.openlcb;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.openlcb.can.OpenLcbCanFrame;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for making CAN frames into OpenLCB messages.
 *
 * @author Bob Jacobsen Copyright 2010
 */
public class CanConverterTest {

    @Test
    public void testCtors() {
        // mostly tests libraries, etc.
        CanMessage t = new CanMessage(0x195B4000);
        assertNotNull(t);

        t = new CanMessage(2, 0x195B4000);
        assertNotNull(t);

        t = new CanMessage(new int[]{1, 2, 3, 4, 5, 6, 7, 8}, 0x182df000);
        assertNotNull(t);

        CanReply r = new CanReply();
        assertNotNull(r);

        r = new CanReply(2);
        assertNotNull(r);

        r = new CanReply(new int[]{1, 2, 3, 4, 5, 6, 7, 8});
        assertNotNull(r);

        OpenLcbCanFrame s = new OpenLcbCanFrame(100);
        assertNotNull(s);
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
