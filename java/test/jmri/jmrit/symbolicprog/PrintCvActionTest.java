package jmri.jmrit.symbolicprog;

import java.awt.event.WindowEvent;

import javax.swing.JLabel;

import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrame;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintCvActionTest {

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testCTor() {
        jmri.Programmer p = jmri.InstanceManager.getDefault(jmri.GlobalProgrammerManager.class).getGlobalProgrammer();
        RosterEntry re = new RosterEntry();
        PaneProgFrame pFrame = new PaneProgFrame(null, re,
                "test frame", "programmers/Basic.xml",
                p, false) {
            // dummy implementations
            @Override
            protected javax.swing.JPanel getModePane() {
                return null;
            }
        };
        CvTableModel cvtm = new CvTableModel(new JLabel(), null);
        PrintCvAction t = new PrintCvAction("Test Action", cvtm, pFrame, false, re);
        Assertions.assertNotNull(t, "exists");
        pFrame.dispatchEvent(new WindowEvent(pFrame, WindowEvent.WINDOW_CLOSING));
    }

    @Test
    public void testCvSortOrderVal(){
        Assertions.assertNotEquals(0, PrintCvAction.cvSortOrderVal("187"));
        Assertions.assertNotEquals(0, PrintCvAction.cvSortOrderVal("257.31=8.32=0"));
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
