package jmri.jmrix.cmri.serial.cmrinetmanager;

import jmri.util.JUnitUtil;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import org.junit.*;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of CMRInetMetricsFrame
 *
 * @author	Chuck Catania Copyright (C) 2017, 2018
 */
public class CMRInetMetricsFrameTest extends jmri.util.JmriJFrameTestBase {

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new CMRInetMetricsFrame(new CMRISystemConnectionMemo());
	} 
    }

    @After
    @Override
    public void tearDown() {
        jmri.util.JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }
}
