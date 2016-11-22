/**
 * PacketGenFrameTest.java
 *
 * Description:	tests for the jmri.jmrix.qsi.packetgen.PacketGenFrame class
 *
 * @author	Bob Jacobsen
 */
package jmri.jmrix.qsi.packetgen;

import java.awt.GraphicsEnvironment;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

public class PacketGenFrameTest {

    @Test
    public void testFrameCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PacketGenFrame packetGenFrame = new PacketGenFrame(new jmri.jmrix.qsi.QsiSystemConnectionMemo());
        Assert.assertNotNull(packetGenFrame);
    }

}
