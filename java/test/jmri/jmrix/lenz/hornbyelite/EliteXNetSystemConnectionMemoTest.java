package jmri.jmrix.lenz.hornbyelite;

import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * EliteXNetSystemConnectionMemoTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.EliteXNetSystemConnectionMemo class
 *
 * @author	Paul Bender
 */
public class EliteXNetSystemConnectionMemoTest {

    @Test
    public void testCtor() {
        // infrastructure objects
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new HornbyEliteCommandStation());

        EliteXNetSystemConnectionMemo t = new EliteXNetSystemConnectionMemo(tc);
        Assert.assertNotNull(t);
        Assert.assertNotNull(t.getXNetTrafficController());
        // While we are constructing the memo, we should also set the 
        // SystemMemo parameter in the traffic controller.
        Assert.assertNotNull(tc.getSystemConnectionMemo());
    }

    @Test
    public void testXNetTrafficControllerSetCtor() {
        // infrastructure objects
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new HornbyEliteCommandStation());

        EliteXNetSystemConnectionMemo t = new EliteXNetSystemConnectionMemo();
        Assert.assertNotNull(t);
        // the default constructor does not set the traffic controller
        Assert.assertNull(t.getXNetTrafficController());
        // so we need to do this ourselves.
        t.setXNetTrafficController(tc);
        Assert.assertNotNull(t.getXNetTrafficController());
        // and while we're doing that, we should also set the SystemMemo 
        // parameter in the traffic controller.
        Assert.assertNotNull(tc.getSystemConnectionMemo());
    }

    @Test
    public void testProivdesConsistManager() {
        // infrastructure objects
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new HornbyEliteCommandStation());

        EliteXNetSystemConnectionMemo t = new EliteXNetSystemConnectionMemo();
        t.setXNetTrafficController(tc);
        t.setCommandStation(tc.getCommandStation());
        Assert.assertFalse(t.provides(jmri.ConsistManager.class));
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
