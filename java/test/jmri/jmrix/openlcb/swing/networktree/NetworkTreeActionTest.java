package jmri.jmrix.openlcb.swing.networktree;

import java.awt.GraphicsEnvironment;

import jmri.InstanceManager;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.openlcb.OlcbSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

import jmri.jmrix.can.TestTrafficController;
import org.mockito.Mockito;

/**
 * @author Bob Jacobsen Copyright 2013
 * @author Paul Bender Copyright(C) 2016
 */
public class NetworkTreeActionTest {

    jmri.jmrix.can.CanSystemConnectionMemo memo;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NetworkTreeAction h = new NetworkTreeAction();
        Assert.assertNotNull("Action object non-null", h);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        memo = Mockito.mock(OlcbSystemConnectionMemo.class);
        InstanceManager.setDefault(CanSystemConnectionMemo.class,memo);
    }

    @AfterEach
    public void tearDown() {
        memo = null;
        JUnitUtil.tearDown();
    }
}
