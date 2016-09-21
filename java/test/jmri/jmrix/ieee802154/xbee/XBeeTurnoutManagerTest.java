package jmri.jmrix.ieee802154.xbee;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import org.powermock.api.mockito.mockpolicies.Slf4jMockPolicy;
import org.powermock.core.classloader.annotations.MockPolicy;
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
public class XBeeTurnoutManagerTest {

    XBeeTrafficController tc = null;

    @Test
    public void testCtor() {
        XBeeTurnoutManager m = new XBeeTurnoutManager(tc, "ABC");
        Assert.assertNotNull("exists", m);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        //apps.tests.Log4JFixture.setUp();
        XBeeTrafficController tc = new XBeeInterfaceScaffold();
        XBeeConnectionMemo m = new XBeeConnectionMemo();
        m.setSystemPrefix("ABC");
        tc.setAdapterMemo(m);
    }

    @After
    public void tearDown() {
        //apps.tests.Log4JFixture.tearDown();
    }

}
