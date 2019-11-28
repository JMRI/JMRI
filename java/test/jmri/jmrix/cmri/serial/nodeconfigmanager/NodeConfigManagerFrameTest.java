package jmri.jmrix.cmri.serial.nodeconfigmanager;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of NodeConfigFrame
 * Copied from NodeConfig
 *
 * @author	Paul Bender Copyright (C) 2016
 * @author	Chuck Catania Copyright (C) 2017
 */
public class NodeConfigManagerFrameTest extends jmri.util.JmriJFrameTestBase {

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new NodeConfigManagerFrame(new CMRISystemConnectionMemo());
    	}
    }

    @After
    @Override
    public void tearDown() {
        jmri.util.JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
    	super.tearDown();
    }
}
