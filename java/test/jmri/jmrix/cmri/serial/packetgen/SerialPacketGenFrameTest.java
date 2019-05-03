package jmri.jmrix.cmri.serial.packetgen;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.*;

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
           frame = new SerialPacketGenFrame(new CMRISystemConnectionMemo()); 
	}
    }

    @After
    @Override
    public void tearDown() {
    	   super.tearDown();
    }
}
