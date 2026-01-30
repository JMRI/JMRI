package jmri.jmrit.symbolicprog.tabbedframe;

import java.awt.event.WindowEvent;

import javax.swing.JPanel;

import jmri.jmrit.roster.RosterEntry;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PaneSetTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {

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
        JUnitUtil.waitFor(()->{return pc.threadCount.get() == 0;}, "PaneProgFrame threads done");

        PaneSet t = new PaneSet(pc, re, p);
        Assertions.assertNotNull( t, "exists");
        JUnitUtil.waitFor(10);
        new org.netbeans.jemmy.QueueTool().waitEmpty();
        pc.dispatchEvent(new WindowEvent(pc, WindowEvent.WINDOW_CLOSING));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugProgrammerManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

}
