package jmri.jmrix.sprog.sprogslotmon;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import jmri.jmrix.sprog.SprogTrafficControlScaffold;

/**
 * Test simple functioning of SprogSlotMonDataModel 
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SprogSlotMonDataModelTest {

    SprogSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        SprogSlotMonDataModel action = new SprogSlotMonDataModel(jmri.jmrix.sprog.SprogConstants.MAX_SLOTS,8,memo);
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        memo = new jmri.jmrix.sprog.SprogSystemConnectionMemo();
        memo.setSprogTrafficController(new SprogTrafficControlScaffold(memo));
        memo.setSprogMode(jmri.jmrix.sprog.SprogConstants.SprogMode.OPS);
        memo.configureCommandStation();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }
}
