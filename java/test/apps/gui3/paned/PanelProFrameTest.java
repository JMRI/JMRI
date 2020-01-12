package apps.gui3.paned;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PanelProFrameTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PanelProFrame t = new PanelProFrame("test");
        Assert.assertNotNull("exists", t);
        t.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        
        // The class under test uses two LocoNet connections it pulls from the InstanceManager.
        jmri.jmrix.loconet.LocoNetSystemConnectionMemo memo = new jmri.jmrix.loconet.LocoNetSystemConnectionMemo();
        jmri.jmrix.loconet.LocoNetInterfaceScaffold lnis = new jmri.jmrix.loconet.LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);
        memo.configureCommandStation(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_DCS100, false, false, false);
        memo.configureManagers();
        jmri.InstanceManager.store(memo, jmri.jmrix.loconet.LocoNetSystemConnectionMemo.class);

        jmri.jmrix.loconet.LocoNetSystemConnectionMemo memo2 = new jmri.jmrix.loconet.LocoNetSystemConnectionMemo();
        jmri.jmrix.loconet.LocoNetInterfaceScaffold lnis2 = new jmri.jmrix.loconet.LocoNetInterfaceScaffold(memo2);
        memo2.setLnTrafficController(lnis2);
        memo2.configureCommandStation(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_DCS100, false, false, false);
        memo2.configureManagers();
        jmri.InstanceManager.store(memo2, jmri.jmrix.loconet.LocoNetSystemConnectionMemo.class);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PanelProFrameTest.class);

}
