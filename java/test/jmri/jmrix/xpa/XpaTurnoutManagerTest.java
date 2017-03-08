package jmri.jmrix.xpa;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XpaTurnoutManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.xpa.XpaTurnoutManager class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class XpaTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    private XpaSystemConnectionMemo memo = null;

    @Override
    public String getSystemName(int i) {
        return "PT" + i;
    }


    @Test
    public void testCtor() {
        Assert.assertNotNull(l);
    }

    // The minimal setup for log4J
    @Override
    @Before 
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        memo = new XpaSystemConnectionMemo();
        memo.setXpaTrafficController(new XpaTrafficController());
        l = new XpaTurnoutManager(memo);
    }

    @After 
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
        memo = null;
        l = null;
    }

}
