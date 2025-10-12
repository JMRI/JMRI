package jmri.jmrix.dccpp.swing;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.jmrix.dccpp.DCCppCommandStation;
import jmri.jmrix.dccpp.DCCppInterfaceScaffold;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DCCppMenuTest {

    private DCCppInterfaceScaffold tc = null;
    private DCCppSystemConnectionMemo memo = null;

    @Test
    public void testCTor() {
        // infrastructure objects
        DCCppMenu t = new DCCppMenu(memo);
        assertNotNull( t, "exists");
    }

    @Test
    public void test2ParamDCCppMenuCTor() {
        // infrastructure objects
        DCCppMenu t = new DCCppMenu("DCc++ test",memo);
        assertNotNull( t, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new DCCppInterfaceScaffold(new DCCppCommandStation());
        memo = new DCCppSystemConnectionMemo(tc);
    }

    @AfterEach
    public void tearDown() {
        memo.getDCCppTrafficController().terminateThreads();
        memo.dispose();
        memo = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(DCCppMenuTest.class);

}
