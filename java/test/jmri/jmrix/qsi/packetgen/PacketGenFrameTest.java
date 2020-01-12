package jmri.jmrix.qsi.packetgen;

import java.awt.GraphicsEnvironment;
import org.junit.*;

/**
 * Tests for the jmri.jmrix.qsi.packetgen.PacketGenFrame class
 *
 * @author	Bob Jacobsen
 */
public class PacketGenFrameTest extends jmri.util.JmriJFrameTestBase {

    @Before
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new PacketGenFrame(new jmri.jmrix.qsi.QsiSystemConnectionMemo());
    	}
    }

    @After
    @Override
    public void tearDown() {
        jmri.util.JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
	    super.tearDown();
    }
}
