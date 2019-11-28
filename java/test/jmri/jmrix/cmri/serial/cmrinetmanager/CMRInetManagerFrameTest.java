package jmri.jmrix.cmri.serial.cmrinetmanager;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of CMRInetManagerFrame
 *
 * @author	Chuck Catania Copyright (C) 2017
 */
public class CMRInetManagerFrameTest extends jmri.util.JmriJFrameTestBase {

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new CMRInetManagerFrame(new CMRISystemConnectionMemo()); 
	}
    }

    @After
    @Override
    public void tearDown() {
        jmri.util.JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }
}
