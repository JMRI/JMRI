package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * DCCppSystemConnectionMemoTest.java
 *
 * Description:	tests for the jmri.jmrix.dccpp.DCCppSystemConnectionMemo class
 *
 * @author	Paul Bender
 * @author	Mark Underwood (C) 2015
 */
public class DCCppSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

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
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());

        DCCppSystemConnectionMemo t = new DCCppSystemConnectionMemo();
        Assert.assertNotNull(t);
        // the default constructor does not set the traffic controller
        Assert.assertNull(t.getDCCppTrafficController());
        // so we need to do this ourselves.
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
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());

        DCCppSystemConnectionMemo memo = new DCCppSystemConnectionMemo(tc);
        memo.setTurnoutManager(new DCCppTurnoutManager(tc, memo.getSystemPrefix()));
        memo.setSensorManager(new DCCppSensorManager(tc, memo.getSystemPrefix()));
        memo.setLightManager(new DCCppLightManager(tc, memo.getSystemPrefix()));
        scm = memo;      
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
