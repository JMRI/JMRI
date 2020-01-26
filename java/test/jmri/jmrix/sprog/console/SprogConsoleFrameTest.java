package jmri.jmrix.sprog.console;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import jmri.jmrix.sprog.SprogTrafficControlScaffold;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of SprogConsoleFrame.
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SprogConsoleFrameTest extends jmri.util.JmriJFrameTestBase {

    private SprogTrafficControlScaffold stcs = null;
    private SprogSystemConnectionMemo m = null;

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = new SprogSystemConnectionMemo(jmri.jmrix.sprog.SprogConstants.SprogMode.OPS);
        stcs = new SprogTrafficControlScaffold(m);
        m.setSprogTrafficController(stcs);
        m.configureCommandStation();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new SprogConsoleFrame(m);
	}
    }

    @After
    @Override
    public void tearDown() {
        m.getSlotThread().interrupt();
        stcs.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }

}
