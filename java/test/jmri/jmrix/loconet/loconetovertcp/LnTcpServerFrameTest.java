package jmri.jmrix.loconet.loconetovertcp;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of LnTcpServerFrame
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LnTcpServerFrameTest extends jmri.util.JmriJFrameTestBase {

    private LocoNetInterfaceScaffold lnis;
    private LocoNetSystemConnectionMemo memo;

    @Test
    public void testGetInstance() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LnTcpServerFrame action = LnTcpServerFrame.getDefault();
        Assert.assertNotNull("exists", action);
        action.dispose();
    }

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        memo = new LocoNetSystemConnectionMemo();
        // ensure memo exists in order to later use InstanceManager.getDefault()
        lnis = new LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);
        memo.configureCommandStation(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_DCS100, true, false, true);
        if(!GraphicsEnvironment.isHeadless()){
          frame = LnTcpServerFrame.getDefault();
        }
    }

    @After
    @Override
    public void tearDown() {
        lnis = null;
        memo.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }

}
