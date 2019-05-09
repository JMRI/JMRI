package jmri.jmrix.easydcc.packetgen;

import jmri.jmrix.easydcc.EasyDccSystemConnectionMemo;
import java.awt.GraphicsEnvironment;
import org.junit.*;

/**
 * Tests for the jmri.jmrix.easydcc.packetgen.EasyDccPacketGenFrame
 * class
 *
 * @author	Bob Jacobsen
 */
public class EasyDccPacketGenFrameTest extends jmri.util.JmriJFrameTestBase {

    @Before
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new EasyDccPacketGenFrame(new EasyDccSystemConnectionMemo("E", "EasyDCC via Serial"));
        } 
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }
}
