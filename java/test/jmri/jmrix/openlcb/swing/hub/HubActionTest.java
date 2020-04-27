package jmri.jmrix.openlcb.swing.hub;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrix.can.TestTrafficController;
/**
 * @author Bob Jacobsen Copyright 2013
 * @author Paul Bender Copyright(C) 2016
 */
public class HubActionTest {

    jmri.jmrix.can.CanSystemConnectionMemo memo;
    jmri.jmrix.can.TrafficController tc;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        HubAction h = new HubAction();
        Assert.assertNotNull("Action object non-null", h);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        memo  = new jmri.jmrix.openlcb.OlcbSystemConnectionMemo();
        tc = new TestTrafficController();
        memo.setTrafficController(tc);
        jmri.InstanceManager.setDefault(CanSystemConnectionMemo.class,memo);
    }

    @After
    public void tearDown() {
        memo.dispose();
        memo = null;
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();

    }
}
