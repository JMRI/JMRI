package jmri.jmrit.symbolicprog.tabbedframe;

import java.awt.GraphicsEnvironment;
import java.awt.event.WindowEvent;

import javax.swing.JPanel;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PaneSetTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.Programmer p = jmri.InstanceManager.getDefault(jmri.AddressedProgrammerManager.class).getAddressedProgrammer(false, 42);
        RosterEntry re = new RosterEntry();
        PaneProgFrame pc = new PaneProgFrame(null, re,
                "test frame", "programmers/Basic.xml",
                p, false) {
            // dummy implementations
            @Override
            protected JPanel getModePane() {
                return null;
            }
        };
        PaneSet t = new PaneSet(pc, re, p);
        Assert.assertNotNull("exists", t);
        new org.netbeans.jemmy.QueueTool().waitEmpty(10);
        pc.dispatchEvent(new WindowEvent(pc, WindowEvent.WINDOW_CLOSING));
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugProgrammerManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
