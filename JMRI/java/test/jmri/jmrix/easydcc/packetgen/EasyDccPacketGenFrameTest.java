package jmri.jmrix.easydcc.packetgen;

import jmri.jmrix.easydcc.EasyDccSystemConnectionMemo;
import java.awt.GraphicsEnvironment;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.easydcc.packetgen.EasyDccPacketGenFrame
 * class
 *
 * @author	Bob Jacobsen
 */
public class EasyDccPacketGenFrameTest {

    @Test
    public void testFrameCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EasyDccPacketGenFrame easyDccPacketGenFrame = new EasyDccPacketGenFrame(new EasyDccSystemConnectionMemo("E", "EasyDCC via Serial"));
        Assert.assertNotNull(easyDccPacketGenFrame);
    }

}
