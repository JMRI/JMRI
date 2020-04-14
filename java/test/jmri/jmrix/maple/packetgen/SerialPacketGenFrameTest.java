package jmri.jmrix.maple.packetgen;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;
import jmri.jmrix.maple.MapleSystemConnectionMemo;

/**
 * Test simple functioning of SerialPacketGenFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SerialPacketGenFrameTest extends jmri.util.JmriJFrameTestBase {

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new SerialPacketGenFrame(new MapleSystemConnectionMemo());
	} 
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
    	super.tearDown();
    }
}
