package jmri.jmrix.ieee802154.xbee;

import jmri.Light;
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
 * XBeeLightManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.xbee.XBeeLightManager class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
@RunWith(PowerMockRunner.class)
public class XBeeLightManagerTest extends jmri.managers.AbstractLightMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return "ABCL2:" + i;
    }


    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", l);
    }

    @Override
    @Test
    public void testDefaultSystemName() {
        // create
        Light t = l.provideLight("ABCL2:" + getNumToTest1());
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Override
    @Test
    public void testUpperLower() {
        Light t = l.provideLight("ABCL2:" + getNumToTest2());
        String name = t.getSystemName();
        Assert.assertNull(l.getLight(name.toLowerCase()));
    }

    // from here down is testing infrastructure
    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        //apps.tests.Log4JFixture.setUp();
        XBeeTrafficController tc = new XBeeInterfaceScaffold();
        XBeeConnectionMemo m = new XBeeConnectionMemo();
        m.setSystemPrefix("ABC");
        tc.setAdapterMemo(m);
        l = new XBeeLightManager(tc, "ABC");
        m.setLightManager(l);
    }

    @After
    public void tearDown() {
        //apps.tests.Log4JFixture.tearDown();
    }

    /**
     * Number of light to test. Made a separate method so it can be overridden
     * in subclasses that do or don't support various numbers
     */
    @Override
    protected int getNumToTest1() {
        return 2;
    }

    @Override
    protected int getNumToTest2() {
        return 7;
    }



}
