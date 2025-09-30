package jmri.jmrix.dccpp.swing;

import jmri.jmrix.dccpp.DCCppCommandStation;
import jmri.jmrix.dccpp.DCCppInterfaceScaffold;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfHeadless
public class ConfigBaseStationFrameTest extends jmri.util.JmriJFrameTestBase {

    private DCCppSystemConnectionMemo memo;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());
        memo = new DCCppSystemConnectionMemo(tc);
        frame = new ConfigBaseStationFrame(memo);

    }

    @AfterEach
    @Override
    public void tearDown() {
        memo.getDCCppTrafficController().terminateThreads();
        memo.dispose();
        memo = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ConfigBaseStationFrameTest.class);
}
