package jmri.jmrix.easydcc.packetgen;

import java.awt.GraphicsEnvironment;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 * EasyDccPacketGenFrameTest.java
 *
 * Description:	tests for the jmri.jmrix.nce.packetgen.EasyDccPacketGenFrame
 * class
 *
 * @author	Bob Jacobsen
 */
public class EasyDccPacketGenFrameTest {

    @Test
    public void testFrameCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EasyDccPacketGenFrame easyDccPacketGenFrame = new EasyDccPacketGenFrame();
        Assert.assertNotNull(easyDccPacketGenFrame);
    }

}
