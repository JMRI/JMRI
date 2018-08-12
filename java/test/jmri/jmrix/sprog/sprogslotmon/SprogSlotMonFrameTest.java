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

    private SprogTrafficControlScaffold stcs = null;
    SprogSystemConnectionMemo m = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SprogSlotMonFrame action = new SprogSlotMonFrame(m);
        Assert.assertNotNull("exists", action);
        action.dispose();
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();

        m = new jmri.jmrix.sprog.SprogSystemConnectionMemo(jmri.jmrix.sprog.SprogConstants.SprogMode.OPS);
        stcs = new SprogTrafficControlScaffold(m);
        m.setSprogTrafficController(stcs);
        m.configureCommandStation();
    }

    @After
    public void tearDown() {
        m.getSlotThread().interrupt();
        stcs.dispose();
        JUnitUtil.tearDown();
    }
}
