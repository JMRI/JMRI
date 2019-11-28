package jmri.jmrix.lenz.swing.lzv100;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * LZV100FrameTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.swing.lzv100.LZV100Frame class
 *
 * @author	Paul Bender
 */
public class LZV100FrameTest extends jmri.util.JmriJFrameTestBase {
        
    private XNetInterfaceScaffold tc;

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        tc = new XNetInterfaceScaffold(new LenzCommandStation());
        if(!GraphicsEnvironment.isHeadless()){
           frame = new LZV100Frame(new XNetSystemConnectionMemo(tc));
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
