package jmri.jmrit.symbolicprog;

import java.awt.event.WindowEvent;

import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrame;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintActionTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {

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
        JUnitUtil.waitFor(()->{return pFrame.threadCount.get() == 0;}, "PaneProgFrame threads done");

        PrintAction t = new PrintAction("Test Action", pFrame, true);
        Assertions.assertNotNull( t, "exists");
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
        JUnitUtil.resetWindows(false, false); // Detachable frame : "Comments : test frame"
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintActionTest.class.getName());
}
