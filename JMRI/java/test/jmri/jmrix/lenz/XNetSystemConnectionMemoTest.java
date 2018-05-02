package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * XNetSystemConnectionMemoTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.XNetSystemConnectionMemo class
 *
 * @author	Paul Bender
 */
public class XNetSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    @Test
    @Override
    public void testCtor() {
        XNetSystemConnectionMemo t = (XNetSystemConnectionMemo)scm;
        Assert.assertNotNull(t);
        Assert.assertNotNull(t.getXNetTrafficController());
        // While we are constructing the memo, we should also set the 
        // SystemMemo parameter in the traffic controller.
        Assert.assertNotNull(t.getXNetTrafficController().getSystemConnectionMemo());
    }

    @Test
    public void testXNetTrafficControllerSetCtor() {
        // infrastructure objects
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());

        XNetSystemConnectionMemo t = new XNetSystemConnectionMemo();
        Assert.assertNotNull(t);
        // the default constructor does not set the traffic controller
        Assert.assertNull(t.getXNetTrafficController());
        // so we need to do this ourselves.
        t.setXNetTrafficController(tc);
        Assert.assertNotNull(t.getXNetTrafficController());
        // and while we're doing that, we should also set the SystemMemo 
        // parameter in the traffic controller.
        Assert.assertNotNull(t.getXNetTrafficController().getSystemConnectionMemo());
    }

    @Test
    public void testProivdesConsistManagerMultiMaus() {
        // infrastructure objects
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation(){
          @Override
          public int getCommandStationType(){
              return(0x10); // MultiMaus
          }
        });

        XNetSystemConnectionMemo t = new XNetSystemConnectionMemo();
        t.setXNetTrafficController(tc);
        t.setCommandStation(tc.getCommandStation());
        Assert.assertFalse(t.provides(jmri.ConsistManager.class));
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation(){
          @Override
          public int getCommandStationType(){
              return(0x00); // LZV100
          }
        });

        XNetSystemConnectionMemo memo = new XNetSystemConnectionMemo(tc);
        memo.setSensorManager(new XNetSensorManager(tc,memo.getSystemPrefix()));
        memo.setLightManager(new XNetLightManager(tc,memo.getSystemPrefix()));
        memo.setTurnoutManager(new XNetTurnoutManager(tc,memo.getSystemPrefix()));
        scm = memo;
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
