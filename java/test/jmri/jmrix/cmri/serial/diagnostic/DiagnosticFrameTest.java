package jmri.jmrix.cmri.serial.diagnostic;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of DiagnosticFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class DiagnosticFrameTest extends jmri.util.JmriJFrameTestBase {

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new DiagnosticFrame(new CMRISystemConnectionMemo()); 
	}
    }

    @After
    @Override
    public void tearDown() {
        jmri.util.JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
	    super.tearDown();
    }
}
