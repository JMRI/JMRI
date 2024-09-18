package jmri.jmrix.can.cbus;

import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Integration Tests for CBUS Cab Signals
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusCabSignalIT extends jmri.implementation.DefaultCabSignalIT {

    private CanSystemConnectionMemo memo = null;
    private TrafficController tc = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();
        InstanceManager.setDefault(jmri.BlockManager.class,new jmri.BlockManager());
        JUnitUtil.initLayoutBlockManager();
        JUnitUtil.initDefaultSignalMastManager();
        JUnitUtil.initSignalMastLogicManager();

        // prepare the cab signal
        memo = new CanSystemConnectionMemo();
        tc = new TrafficControllerScaffold();
        memo.setTrafficController(tc);

        cs = new CbusCabSignal(memo,new DccLocoAddress(1234,true));
    }

    @AfterEach
    @Override
    public void tearDown() {
        Assertions.assertNotNull(memo);
        memo.dispose();
        memo = null;
        Assertions.assertNotNull(tc);
        tc.terminateThreads();
        tc = null;
        cs.dispose();
        cs = null;
        InstanceManager.getDefault(jmri.IdTagManager.class).dispose();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        jmri.jmrit.display.EditorFrameOperator.clearEditorFrameOperatorThreads();
        JUnitUtil.resetWindows(false, false);
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusCabSignalTest.class);

}
