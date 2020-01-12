package jmri.jmrix.openlcb;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.openlcb.can.OpenLcbCanFrame;

/**
 * Tests for making CAN frames into OpenLCB messages.
 *
 * @author Bob Jacobsen Copyright 2010
 */
public class CanConverterTest {

    @Test
    public void testCtors() {
        // mostly tests libraries, etc.
        new CanMessage(0x195B4000);
        new CanMessage(2, 0x195B4000);
        new CanMessage(new int[]{1, 2, 3, 4, 5, 6, 7, 8}, 0x182df000);
        new CanReply();
        new CanReply(2);
        new CanReply(new int[]{1, 2, 3, 4, 5, 6, 7, 8});
        new OpenLcbCanFrame(100);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
