package jmri.jmrix.openlcb.swing.networktree;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.jmrix.can.TestTrafficController;

/**
 * @author Bob Jacobsen Copyright 2013
 * @author Paul Bender Copyright (C) 2016
 */
public class NetworkTreePaneTest {

    jmri.jmrix.can.CanSystemConnectionMemo memo;
    jmri.jmrix.can.TrafficController tc;

    @Test
    public void testCtor() {
        NetworkTreePane p = new NetworkTreePane();
        Assert.assertNotNull("Pane object non-null", p);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        memo  = new jmri.jmrix.openlcb.OlcbSystemConnectionMemo();
        tc = new TestTrafficController();
        memo.setTrafficController(tc);
    }

    @AfterEach
    public void tearDown() {
        memo.dispose();
        memo = null;
        tc.terminateThreads();
        tc = null;
        jmri.util.JUnitUtil.resetWindows(false, false);
        JUnitUtil.tearDown();

    }
}
