package jmri.jmrix.openlcb.swing.protocoloptions;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.openlcb.swing.protocoloptions package
 *
 * @author Andrew Crosland (C) 2020
 */
public class ProtocolOptionsActionTest {
    
    private TrafficControllerScaffold tcs = null; 
    private CanSystemConnectionMemo memo = null;

    @Test
    public void testCTor() {
        ProtocolOptionsAction p = new ProtocolOptionsAction(memo);
        Assert.assertNotNull("exists",p);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.initConfigureManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        tcs = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tcs);
        jmri.InstanceManager.setDefault(CanSystemConnectionMemo.class,memo);
    }

    @AfterEach
    public void tearDown() {
        memo.dispose();
        memo = null;
        tcs.terminateThreads();
        tcs = null;
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(MonitorActionTest.class);

}
