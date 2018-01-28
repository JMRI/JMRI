package jmri.jmrix.sprog.sprogslotmon;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import jmri.jmrix.sprog.SprogTrafficControlScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of SprogSlotMonDataModel 
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SprogSlotMonDataModelTest {

    private SprogTrafficControlScaffold stcs = null;
    private SprogSystemConnectionMemo m = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        SprogSlotMonDataModel action = new SprogSlotMonDataModel(jmri.jmrix.sprog.SprogConstants.MAX_SLOTS, 8, m);
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        m = new jmri.jmrix.sprog.SprogSystemConnectionMemo(jmri.jmrix.sprog.SprogConstants.SprogMode.OPS);
        stcs = new SprogTrafficControlScaffold(m);
        m.setSprogTrafficController(stcs);
        m.configureCommandStation();
    }

    @After
    public void tearDown() {
        m.getSlotThread().interrupt();
        JUnitUtil.tearDown();
    }
}
