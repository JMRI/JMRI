package jmri.jmrix.sprog;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for SprogTurnoutManager.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SprogTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    private SprogSystemConnectionMemo m = null;

    @Override
    public String getSystemName(int i) {
        return "ST" + i;
    }

    @Test
    public void ConstructorTest() {
        Assert.assertNotNull(l);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        m = new SprogSystemConnectionMemo();
        l = new SprogTurnoutManager(m);
    }

    @After
    public void tearDown() {
        m = null;
        l = null;
        JUnitUtil.tearDown();
    }

}
