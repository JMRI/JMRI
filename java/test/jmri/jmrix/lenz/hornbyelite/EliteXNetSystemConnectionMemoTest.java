package jmri.jmrix.lenz.hornbyelite;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * EliteXNetSystemConnectionMemoTest.java
 * <p>
 * Test for the jmri.jmrix.lenz.EliteXNetSystemConnectionMemo class
 *
 * @author Paul Bender
 */
public class EliteXNetSystemConnectionMemoTest extends SystemConnectionMemoTestBase<EliteXNetSystemConnectionMemo> {

    @Test
    @Override
    public void testCtor() {
        Assert.assertNotNull(scm);
        Assert.assertNotNull(scm.getXNetTrafficController());
        // While we are constructing the memo, we should also set the 
        // SystemMemo parameter in the traffic controller.
        Assert.assertNotNull(scm.getXNetTrafficController().getSystemConnectionMemo());
    }

    @Test
    public void testXNetTrafficControllerSetCtor() {
        // cleanup traffic controller added in setup
        scm.getXNetTrafficController().terminateThreads();

        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new HornbyEliteCommandStation());
        Assert.assertNotNull(scm);
        // the default constructor does not set the traffic controller
        Assert.assertNotEquals(tc, scm.getXNetTrafficController());
        // so we need to do this ourselves.
        scm.setXNetTrafficController(tc);
        Assert.assertNotNull(scm.getXNetTrafficController());
        // and while we're doing that, we should also set the SystemMemo 
        // parameter in the traffic controller.
        Assert.assertNotNull(tc.getSystemConnectionMemo());
    }

    @Override
    @Test
    public void testProvidesConsistManager() {
        scm.setCommandStation(scm.getXNetTrafficController().getCommandStation());
        Assert.assertFalse(scm.provides(jmri.ConsistManager.class));
    }

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new HornbyEliteCommandStation());

        scm = new EliteXNetSystemConnectionMemo(tc);
        scm.setSensorManager(new jmri.jmrix.lenz.XNetSensorManager(scm));
        scm.setLightManager(new jmri.jmrix.lenz.XNetLightManager(scm));
        scm.setTurnoutManager(new EliteXNetTurnoutManager(scm));
    }

    @After
    @Override
    public void tearDown() {
        scm.getXNetTrafficController().terminateThreads();
        scm.dispose();
        JUnitUtil.tearDown();
    }

}
