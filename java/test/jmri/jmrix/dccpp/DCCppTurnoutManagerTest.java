package jmri.jmrix.dccpp;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class DCCppTurnoutManagerTest {
    // Note: this doesn't use the usual test pattern for turnouts, which
    // inherits an extensive set of tests

    @Test
    public void testCTor() {
        // infrastructure objects
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());

        DCCppSystemConnectionMemo memo = new DCCppSystemConnectionMemo(tc);

        DCCppTurnoutManager tm = new DCCppTurnoutManager(memo);
        Assert.assertNotNull("exists",tm);
    }

    @Test
    public void testZeroIsOkAddress() {
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());

        DCCppSystemConnectionMemo memo = new DCCppSystemConnectionMemo(tc);

        DCCppTurnoutManager tm = new DCCppTurnoutManager(memo);
        
        Turnout t = tm.provideTurnout("DT0");
        Assert.assertNotNull("exists",t);
        Assert.assertEquals("DT0", t.getSystemName());
        
    }
    @Test
    public void testAddressFormOK() {
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());

        DCCppSystemConnectionMemo memo = new DCCppSystemConnectionMemo(tc);

        DCCppTurnoutManager tm = new DCCppTurnoutManager(memo);
        
        Turnout t = tm.provideTurnout("DT10");
        Assert.assertNotNull("exists",t);
        Assert.assertEquals("DT10", t.getSystemName());
        
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetWindows(false,false); // shouldn't be necessary, can't see where windows are created
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(DCCppTurnoutManagerTest.class);

}
