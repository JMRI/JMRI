package jmri.jmrix.lenz.hornbyelite;

import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * EliteXNetSystemConnectionMemoTest.java
 * <p>
 * Description:	tests for the jmri.jmrix.lenz.EliteXNetSystemConnectionMemo
 * class
 *
 * @author	Paul Bender
 */
public class EliteXNetSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    @Test
    @Override
    public void testCtor() {
        EliteXNetSystemConnectionMemo t = (EliteXNetSystemConnectionMemo) scm;
        Assert.assertNotNull(t);
        Assert.assertNotNull(t.getXNetTrafficController());
        // While we are constructing the memo, we should also set the 
        // SystemMemo parameter in the traffic controller.
        Assert.assertNotNull(t.getXNetTrafficController().getSystemConnectionMemo());
    }

    @Test
    public void testXNetTrafficControllerSetCtor() {
        EliteXNetSystemConnectionMemo t = (EliteXNetSystemConnectionMemo) scm;
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new HornbyEliteCommandStation());
        Assert.assertNotNull(t);
        // the default constructor does not set the traffic controller
        Assert.assertNotEquals(tc, t.getXNetTrafficController());
        // so we need to do this ourselves.
        t.setXNetTrafficController(tc);
        Assert.assertNotNull(t.getXNetTrafficController());
        // and while we're doing that, we should also set the SystemMemo 
        // parameter in the traffic controller.
        Assert.assertNotNull(tc.getSystemConnectionMemo());
    }

    @Override
    @Test
    public void testProvidesConsistManager() {
        EliteXNetSystemConnectionMemo t = (EliteXNetSystemConnectionMemo) scm;
        t.setCommandStation(t.getXNetTrafficController().getCommandStation());
        Assert.assertFalse(t.provides(jmri.ConsistManager.class));
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new HornbyEliteCommandStation());

        EliteXNetSystemConnectionMemo memo = new EliteXNetSystemConnectionMemo(tc);
        memo.setSensorManager(new jmri.jmrix.lenz.XNetSensorManager(memo));
        memo.setLightManager(new jmri.jmrix.lenz.XNetLightManager(memo));
        memo.setTurnoutManager(new EliteXNetTurnoutManager(memo));
        scm = memo;
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
