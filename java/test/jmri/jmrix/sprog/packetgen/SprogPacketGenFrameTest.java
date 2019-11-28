package jmri.jmrix.sprog.packetgen;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of SprogPacketGenFrame 
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SprogPacketGenFrameTest extends jmri.util.JmriJFrameTestBase {

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new SprogPacketGenFrame(new jmri.jmrix.sprog.SprogSystemConnectionMemo());
	}
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
    	super.tearDown();
    }
}
