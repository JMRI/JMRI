package jmri.jmrit.symbolicprog;

import java.awt.GraphicsEnvironment;
import java.awt.event.WindowEvent;

import javax.swing.JLabel;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrame;
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
public class PrintCvActionTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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
        Assert.assertNotNull("exists", t);
        pFrame.dispatchEvent(new WindowEvent(pFrame, WindowEvent.WINDOW_CLOSING));
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

    // private final static Logger log = LoggerFactory.getLogger(PrintCvActionTest.class.getName());
}
