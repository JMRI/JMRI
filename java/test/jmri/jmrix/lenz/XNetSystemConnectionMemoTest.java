package jmri.jmrix.lenz;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XNetSystemConnectionMemoTest.java
 * <p>
 * Test for the jmri.jmrix.lenz.XNetSystemConnectionMemo class
 *
 * @author Paul Bender
 */
public class XNetSystemConnectionMemoTest extends SystemConnectionMemoTestBase<XNetSystemConnectionMemo> {

    @Test
    @Override
    public void testCtor() {
        Assert.assertNotNull(scm);
        Assert.assertNotNull(scm.getXNetTrafficController());
        // While we are constructing the scm, we should also set the 
        // SystemMemo parameter in the traffic controller.
        Assert.assertNotNull(scm.getXNetTrafficController().getSystemConnectionMemo());
    }

    @Test
    public void testXNetTrafficControllerSetCtor() {
        // infrastructure objects
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        XNetSystemConnectionMemo memo = new XNetSystemConnectionMemo();
        // the default constructor does not set the traffic controller
        Assert.assertNull(memo.getXNetTrafficController());
        // so we need to do this ourselves.
        memo.setXNetTrafficController(tc);
        Assert.assertNotNull(memo.getXNetTrafficController());
        // and while we're doing that, we should also set the SystemConnectionMemo
        // parameter in the traffic controller.
        Assert.assertNotNull(memo.getXNetTrafficController().getSystemConnectionMemo());
        // cleanup traffic controller
        memo.getXNetTrafficController().terminateThreads();
    }

    @Test
    public void testProivdesConsistManagerMultiMaus() {
        // cleanup traffic controller from setup
        scm.getXNetTrafficController().terminateThreads();
        // infrastructure objects
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation() {
            @Override
            public int getCommandStationType() {
                return (0x10); // MultiMaus
            }
        });

        scm.setXNetTrafficController(tc);
        scm.setCommandStation(tc.getCommandStation());
        Assert.assertFalse(scm.provides(jmri.ConsistManager.class));
    }

    @Test
    public void testProivdesCommandStaitonCompact() {
        // cleanup traffic controller from setup
        scm.getXNetTrafficController().terminateThreads();
        // infrastructure objects
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation() {
            @Override
            public int getCommandStationType() {
                return (0x02); // Lenz Compact/Atlas Commander
            }
        });

        scm.setXNetTrafficController(tc);
        scm.setCommandStation(tc.getCommandStation());
        Assert.assertFalse(scm.provides(jmri.CommandStation.class));
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation() {
            @Override
            public int getCommandStationType() {
                return (0x00); // LZV100
            }
        });

        scm = new XNetSystemConnectionMemo(tc);
        scm.setSensorManager(new XNetSensorManager(scm));
        scm.setLightManager(new XNetLightManager(scm));
        scm.setTurnoutManager(new XNetTurnoutManager(scm));
    }

    @After
    @Override
    public void tearDown() {
        scm.getXNetTrafficController().terminateThreads();
        scm.dispose();
        JUnitUtil.tearDown();
    }

}
