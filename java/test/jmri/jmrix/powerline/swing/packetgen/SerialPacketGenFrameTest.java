    package jmri.jmrix.powerline.swing.packetgen;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.powerline.SerialTrafficControlScaffold;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of SerialPacketGenFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SerialPacketGenFrameTest extends jmri.util.JmriJFrameTestBase {

    private SerialTrafficControlScaffold tc = null;

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new SerialTrafficControlScaffold();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new SerialPacketGenFrame(tc);
	}
    }

    @After
    @Override
    public void tearDown() {
	    tc = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
    	super.tearDown();
    }

}
