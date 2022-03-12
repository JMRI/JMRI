package jmri.jmrit.symbolicprog;

import java.awt.GraphicsEnvironment;
import java.awt.event.WindowEvent;

import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrame;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintActionTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.Programmer p = jmri.InstanceManager.getDefault(jmri.GlobalProgrammerManager.class).getGlobalProgrammer();
        PaneProgFrame pFrame = new PaneProgFrame(null, new RosterEntry(),
                "test frame", "programmers/Basic.xml",
                p, false) {
            // dummy implementations
            @Override
            protected javax.swing.JPanel getModePane() {
                return null;
            }
        };
        PrintAction t = new PrintAction("Test Action", pFrame, true);
        Assert.assertNotNull("exists", t);
        pFrame.dispatchEvent(new WindowEvent(pFrame, WindowEvent.WINDOW_CLOSING));
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

    // private final static Logger log = LoggerFactory.getLogger(PrintActionTest.class.getName());
}
