package jmri.jmrix.xpa.swing.xpaconfig;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * @author Paul Bender Copyright(C) 2016
 */
public class XpaConfigureFrameTest extends jmri.util.JmriJFrameTestBase {

    private jmri.jmrix.xpa.XpaSystemConnectionMemo memo = null;

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();

        memo = new jmri.jmrix.xpa.XpaSystemConnectionMemo();
        jmri.InstanceManager.setDefault(jmri.jmrix.xpa.XpaSystemConnectionMemo.class,memo);
        if(!GraphicsEnvironment.isHeadless()){
           frame = new XpaConfigureFrame(memo);
	}
    }

    @After
    @Override
    public void tearDown() {
	    memo = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }
}
