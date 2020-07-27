package jmri.jmrix.dccpp;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.dccpp.DCCppSystemConnectionMemo class.
 *
 * @author Paul Bender
 * @author Mark Underwood (C) 2015
 */
public class DCCppSystemConnectionMemoTest extends SystemConnectionMemoTestBase<DCCppSystemConnectionMemo> {

    @Override
    @Test
    public void testCtor() {
        Assert.assertNotNull(scm);
        Assert.assertNotNull(scm.getDCCppTrafficController());
        // While we are constructing the scm, we should also set the 
        // SystemMemo parameter in the traffic controller.
        Assert.assertNotNull(scm.getDCCppTrafficController().getSystemConnectionMemo());
    }

    @Test
    public void testDCCppTrafficControllerSetCtor() {
        // cleanup traffic controller from setup
        scm.getDCCppTrafficController().terminateThreads();
        // infrastructure objects
        DCCppTrafficController tc = new DCCppInterfaceScaffold(new DCCppCommandStation());

        // the default constructor does not set the traffic controller - now it does
        Assert.assertNotNull(scm.getDCCppTrafficController());
        // but we want to replace it with something special so we need to do this ourselves.
        scm.setDCCppTrafficController(tc);
        Assert.assertNotNull(scm.getDCCppTrafficController());
        // and while we're doing that, we should also set the SystemMemo 
        // parameter in the traffic controller.
        Assert.assertNotNull(tc.getSystemConnectionMemo());
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        scm = new DCCppSystemConnectionMemo(new DCCppInterfaceScaffold(new DCCppCommandStation()));
        scm.setTurnoutManager(new DCCppTurnoutManager(scm));
        scm.setSensorManager(new DCCppSensorManager(scm));
        scm.setLightManager(new DCCppLightManager(scm));
    }

    @Override
    @AfterEach
    public void tearDown() {
        scm.getDCCppTrafficController().terminateThreads();
        scm.dispose();
        JUnitUtil.tearDown();
    }

}
