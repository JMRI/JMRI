package jmri.jmrix.qsi.packetgen;

import java.awt.GraphicsEnvironment;
import org.junit.*;

/**
 * Tests for the jmri.jmrix.qsi.packetgen.PacketGenFrame class
 *
 * @author	Bob Jacobsen
 */
public class PacketGenFrameTest {

    @Test
    public void testFrameCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PacketGenFrame packetGenFrame = new PacketGenFrame(new jmri.jmrix.qsi.QsiSystemConnectionMemo());
        Assert.assertNotNull(packetGenFrame);
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

}
