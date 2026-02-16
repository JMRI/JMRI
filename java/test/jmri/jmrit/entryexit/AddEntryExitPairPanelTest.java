package jmri.jmrit.entryexit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.util.HashMap;

import jmri.JmriException;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTableOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Dave Sand Copyright (C) 2018
 */
@DisabledIfHeadless
public class AddEntryExitPairPanelTest {

    private HashMap<String, LayoutEditor> panels = new HashMap<>();

    @Test
    public void testCTor() {

        AddEntryExitPairPanel t = new AddEntryExitPairPanel(panels.get("Alpha"));  // NOI18N
        assertNotNull(t, "exists");
    }

    @Test
    public void testPanelActions() {
        assumeFalse(Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"),
            "Ignoring intermittent test");

        // Open the NX window
        AddEntryExitPairAction nxAction = new AddEntryExitPairAction("ENTRY EXIT", panels.get("Alpha"));  // NOI18N
        nxAction.actionPerformed(null);
        JFrameOperator nxFrame = new JFrameOperator(Bundle.getMessage("AddEntryExitPoints"));  // NOI18N
        assertNotNull(nxFrame, "nxFrame");
        JTableOperator tbl = new JTableOperator(nxFrame, 0);
        assertEquals(6, tbl.getModel().getRowCount(), "Initial row count");

        // Add new pair: NX-AE-Main to NX-To-Beta
        new JComboBoxOperator(nxFrame, 1).selectItem("NX-AE-Main");  // NOI18N
        new JComboBoxOperator(nxFrame, 2).selectItem("NX-To-Beta");  // NOI18N
        new JComboBoxOperator(nxFrame, 3).selectItem(Bundle.getMessage("FullInterlock"));  // NOI18N
        new JButtonOperator(nxFrame, Bundle.getMessage("AddPair")).push();  // NOI18N
        assertEquals(7, tbl.getModel().getRowCount(), "After add row count");

        // Delete a NX pair
        tbl.clickOnCell(3, 5);
        assertEquals(6, tbl.getModel().getRowCount(), "After delete row count");

        // Open the Options window - we've commented out the Jemmy approach
        //  String[] optionPath = {"Options", "Options"};  // NOI18N
        //  JMenuBarOperator nxMenu = new JMenuBarOperator(nxFrame);
        //  nxMenu.getTimeouts().setTimeout("JMenuOperator.WaitBeforePopupTimeout", 30L);
        //  nxMenu.pushMenu(optionPath);
        // and are doing it directly; see Issue #6081
        java.util.List<AddEntryExitPairFrame>  frames = jmri.util.JmriJFrame.getFrameList(AddEntryExitPairFrame.class);
        assertEquals(1, frames.size(), "Should be only one frame");
        frames.get(0).nxPanel.optionWindow(null);


        // Close the options window
        JFrameOperator optionFrame = new JFrameOperator(Bundle.getMessage("OptionsTitle"));  // NOI18N
        assertNotNull(optionFrame, "optionFrame");
        new JButtonOperator(optionFrame, Bundle.getMessage("ButtonOK")).push();  // NOI18N
        optionFrame.dispose();

        nxFrame.dispose();
    }

    @BeforeEach
    public void setUp() throws JmriException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        panels = EntryExitTestTools.getPanels();
        assertEquals(2, panels.size(), "Get LE panels");
    }

    @AfterEach
    public void tearDown() {
        if (panels != null) {
            panels.forEach((name, panel) -> JUnitUtil.dispose(panel));
        }
        panels = null;

        JUnitUtil.clearTurnoutThreads();
        JUnitUtil.clearRouteThreads();
        JUnitUtil.removeMatchingThreads("Routing stabilising timer");

        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AddEntryExitPairPanelTest.class);

}
