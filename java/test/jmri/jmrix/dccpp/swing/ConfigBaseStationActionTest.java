package jmri.jmrix.dccpp.swing;

import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfHeadless
public class ConfigBaseStationActionTest {

    private DCCppSystemConnectionMemo _memo;

    @Test
    public void testCTor() {
        ConfigBaseStationAction action = new ConfigBaseStationAction(); // default memo and title
        Assertions.assertNotNull( action, "exists");
    }

    @Test
    public void testConfigBaseStationActionActionPerformed() {
        ConfigBaseStationAction action = new ConfigBaseStationAction(_memo);
        ThreadingUtil.runOnGUI(() -> action.actionPerformed(null));

        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("FieldManageBaseStationFrameTitle"));
        Assertions.assertNotNull(jfo);

        JUnitUtil.dispose(jfo.getWindow());
        jfo.waitClosed();

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.jmrix.dccpp.DCCppInterfaceScaffold t = new jmri.jmrix.dccpp.DCCppInterfaceScaffold(new jmri.jmrix.dccpp.DCCppCommandStation());
        _memo = new DCCppSystemConnectionMemo(t);
        jmri.InstanceManager.store(_memo, DCCppSystemConnectionMemo.class);
    }

    @AfterEach
    public void tearDown() {
        _memo.getDCCppTrafficController().terminateThreads();
        _memo.dispose();
        _memo = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ConfigBaseStationActionTest.class);

}
