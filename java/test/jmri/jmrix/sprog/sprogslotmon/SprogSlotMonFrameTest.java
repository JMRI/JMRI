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
 * Test simple functioning of SprogSlotMonFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SprogSlotMonFrameTest {

    SprogSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SprogSlotMonFrame action = new SprogSlotMonFrame(memo);
        Assert.assertNotNull("exists", action);
        action.dispose();
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new jmri.jmrix.sprog.SprogSystemConnectionMemo();
        memo.setSprogTrafficController(new SprogTrafficControlScaffold(memo));
        memo.setSprogMode(jmri.jmrix.sprog.SprogConstants.SprogMode.OPS);
        memo.configureCommandStation();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
