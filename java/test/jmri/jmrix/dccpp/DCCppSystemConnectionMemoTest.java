package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.dccpp.DCCppSystemConnectionMemo class.
 *
 * @author	Paul Bender
 * @author	Mark Underwood (C) 2015
 */
public class DCCppSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    DCCppInterfaceScaffold tc;

    @Override
    @Test
    public void testCtor() {
        DCCppSystemConnectionMemo t = (DCCppSystemConnectionMemo) scm;  
        Assert.assertNotNull(t);
        Assert.assertNotNull(t.getDCCppTrafficController());
        // While we are constructing the memo, we should also set the 
        // SystemMemo parameter in the traffic controller.
        Assert.assertNotNull(t.getDCCppTrafficController().getSystemConnectionMemo());
    }

    @Test
    public void testDCCppTrafficControllerSetCtor() {
        // infrastructure objects
        tc = new DCCppInterfaceScaffold(new DCCppCommandStation());

        DCCppSystemConnectionMemo t = new DCCppSystemConnectionMemo();
        Assert.assertNotNull(t);
        // the default constructor does not set the traffic controller - now it does
        Assert.assertNotNull(t.getDCCppTrafficController());
        // but we want to replace it with something special so we need to do this ourselves.
        t.setDCCppTrafficController(tc);
        Assert.assertNotNull(t.getDCCppTrafficController());
        // and while we're doing that, we should also set the SystemMemo 
        // parameter in the traffic controller.
        Assert.assertNotNull(tc.getSystemConnectionMemo());
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        tc = new DCCppInterfaceScaffold(new DCCppCommandStation());

        DCCppSystemConnectionMemo memo = new DCCppSystemConnectionMemo(tc);
        memo.setTurnoutManager(new DCCppTurnoutManager(memo));
        memo.setSensorManager(new DCCppSensorManager(memo));
        memo.setLightManager(new DCCppLightManager(memo));
        scm = memo;      
    }

    @Override
    @After
    public void tearDown() {
        tc.terminateThreads();
        tc = null;
        scm = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
