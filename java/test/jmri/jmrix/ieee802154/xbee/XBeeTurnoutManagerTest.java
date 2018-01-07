package jmri.jmrix.ieee802154.xbee;

import jmri.Turnout;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.mockpolicies.Slf4jMockPolicy;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.modules.junit4.PowerMockRunner;
@MockPolicy(Slf4jMockPolicy.class)

/**
 * XBeeTurnoutManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.xbee.XBeeTurnoutManager
 * class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
@RunWith(PowerMockRunner.class)
public class XBeeTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    XBeeTrafficController tc = null;

    @Override
    public String getSystemName(int i){
       return "ABCT2:" +i;
    }

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", l);
    }

    @Override
    @Test
    public void testDefaultSystemName() {
        // create
        Turnout t = l.provideTurnout("ABCT2:" + getNumToTest1());
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Override
    @Test(expected=IllegalArgumentException.class)
    public void testProvideFailure() {
        l.provideTurnout("");
    }


    @Override
    @Test
    public void testUpperLower() {
        Turnout t = l.provideTurnout("ABCT2:" + getNumToTest2());

        Assert.assertNull(l.getTurnout(t.getSystemName().toLowerCase()));
    }


    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.resetInstanceManager();
        XBeeTrafficController tc = new XBeeInterfaceScaffold();
        XBeeConnectionMemo m = new XBeeConnectionMemo();
        m.setSystemPrefix("ABC");
        tc.setAdapterMemo(m);
        l = new XBeeTurnoutManager(tc, "ABC");
        m.setTurnoutManager(l);
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
    }

}
