package jmri.jmrix.dccpp.swing;

import jmri.jmrix.dccpp.DCCppCommandStation;
import jmri.jmrix.dccpp.DCCppInterfaceScaffold;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
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
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void test2ParamCTor() {
        // infrastructure objects
        DCCppMenu t = new DCCppMenu("DCc++ test",memo);
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new DCCppInterfaceScaffold(new DCCppCommandStation());
        memo = new DCCppSystemConnectionMemo(tc);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(DCCppMenuTest.class);

}
