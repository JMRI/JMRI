package jmri.jmrit.ctc.editor;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;

import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;

import jmri.InstanceManager;
import jmri.profile.NullProfile;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.*;

/**
 * Tests for the CtcEditorAction Class using signal masts.
 *
 * @author Dave Sand Copyright (C) 2019
 */
public class CtcEditorSignalMastsTest {

    JFrameOperator _jfo = null;

    static int DELAY = 0;  // if this is final, get dead code warnings
    static final boolean PAUSE = false;

    @Test
    public void testEditor() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // Load the test panel and initialize Logix and advanced block routing
        java.io.File f = new java.io.File("java/test/jmri/jmrit/ctc/configurexml/load/CTC_Test_Masts-SML.xml");  // NOI18N
        InstanceManager.getDefault(jmri.ConfigureManager.class).load(f);
        InstanceManager.getDefault(jmri.LogixManager.class).activateAllLogixs();
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();
        JUnitUtil.waitFor(5000);     // Wait for block routing and SML initialization


        // Start the Editor
        new CtcEditorAction().actionPerformed(null);

        _jfo = new JFrameOperator("CTC Editor");
        Assert.assertNotNull(_jfo);
        if (PAUSE) JUnitUtil.waitFor(2000);

        // Perform the tests
        frameButtonTests();
        editTests();
        toggleCheckBoxes();
        menuTests();        // Last because File => New wipes out the data.

        _jfo.requestClose();
        _jfo = null;
        if (PAUSE) JUnitUtil.waitFor(2000);
    }

    void menuTests() {
        JMenuBarOperator jmbo = new JMenuBarOperator(_jfo); // there's only one menubar
        JMenuOperator jmo;
        JPopupMenu jpm;

        // ** Edit menu **
        jmo = new JMenuOperator(jmbo, Bundle.getMessage("MenuEdit"));  // NOI18N
        jpm = jmo.getPopupMenu();

        // MenuFix
        JMenuItem fixMenuItem = (JMenuItem) jpm.getComponent(0);
        Assert.assertTrue(fixMenuItem.getText().equals(Bundle.getMessage("MenuFix")));  // NOI18N
        new JMenuItemOperator(fixMenuItem).doClick();
        if (DELAY > 0) {
            new org.netbeans.jemmy.EventTool().waitNoEvent(DELAY);
        }
        JFrameOperator frmFix = new JFrameOperator(Bundle.getMessage("TitleDlgFix"));  // NOI18N
        Assert.assertNotNull(frmFix);
        if (PAUSE) JUnitUtil.waitFor(2000);
        new JButtonOperator(frmFix, Bundle.getMessage("ButtonProceed")).doClick();

        // ** Configure menu **
        jmo = new JMenuOperator(jmbo, Bundle.getMessage("MenuConfigure"));  // NOI18N
        jpm = jmo.getPopupMenu();

        // MenuDebugging
        JMenuItem debugMenuItem = (JMenuItem) jpm.getComponent(0);
        Assert.assertTrue(debugMenuItem.getText().equals(Bundle.getMessage("MenuDebugging")));  // NOI18N
        new JMenuItemOperator(debugMenuItem).doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        JFrameOperator frmDebug = new JFrameOperator(Bundle.getMessage("TitleDlgDeb"));  // NOI18N
        Assert.assertNotNull(frmDebug);
        if (PAUSE) JUnitUtil.waitFor(2000);
        new JButtonOperator(frmDebug, Bundle.getMessage("ButtonSaveClose")).doClick();

        // MenuDefaults
        JMenuItem defaultMenuItem = (JMenuItem) jpm.getComponent(1);
        Assert.assertTrue(defaultMenuItem.getText().equals(Bundle.getMessage("MenuDefaults")));  // NOI18N
        new JMenuItemOperator(defaultMenuItem).doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        JFrameOperator frmDefaults = new JFrameOperator(Bundle.getMessage("TitleDlgDef"));  // NOI18N
        Assert.assertNotNull(frmDefaults);
        if (PAUSE) JUnitUtil.waitFor(2000);
        new JButtonOperator(frmDefaults, Bundle.getMessage("ButtonSaveClose")).doClick();

        // MenuFleeting
        JMenuItem fleetMenuItem = (JMenuItem) jpm.getComponent(2);
        Assert.assertTrue(fleetMenuItem.getText().equals(Bundle.getMessage("MenuFleeting")));  // NOI18N
        new JMenuItemOperator(fleetMenuItem).doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        JFrameOperator frmFleeting = new JFrameOperator(Bundle.getMessage("TitleDlgFleet"));  // NOI18N
        Assert.assertNotNull(frmFleeting);
        if (PAUSE) JUnitUtil.waitFor(2000);
        new JButtonOperator(frmFleeting, Bundle.getMessage("ButtonSaveClose")).doClick();

        // MenuPatterns
        JMenuItem patternMenuItem = (JMenuItem) jpm.getComponent(3);
        Assert.assertTrue(patternMenuItem.getText().equals(Bundle.getMessage("MenuPatterns")));  // NOI18N
        new JMenuItemOperator(patternMenuItem).doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        JFrameOperator frmPatterns = new JFrameOperator(Bundle.getMessage("TItleDlgPat"));  // NOI18N
        Assert.assertNotNull(frmPatterns);
        if (PAUSE) JUnitUtil.waitFor(2000);
        new JButtonOperator(frmPatterns, Bundle.getMessage("ButtonSaveClose")).doClick();

        // MenuDesign
        JMenuItem designMenuItem = (JMenuItem) jpm.getComponent(4);
        Assert.assertTrue(designMenuItem.getText().equals(Bundle.getMessage("MenuDesign")));  // NOI18N
        new JMenuItemOperator(designMenuItem).doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        JFrameOperator frmGUI = new JFrameOperator(Bundle.getMessage("TitleDlgGUI"));  // NOI18N
        Assert.assertNotNull(frmGUI);
        if (PAUSE) JUnitUtil.waitFor(2000);
        new JButtonOperator(frmGUI, Bundle.getMessage("ButtonSaveClose")).doClick();

        // ** About menu **
        jmo = new JMenuOperator(jmbo, Bundle.getMessage("MenuAbout"));  // NOI18N
        jpm = jmo.getPopupMenu();

        // MenuAbout
        JMenuItem aboutMenuItem = (JMenuItem) jpm.getComponent(0);
        Assert.assertTrue(aboutMenuItem.getText().equals(Bundle.getMessage("MenuAbout")));  // NOI18N
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        new JMenuItemOperator(aboutMenuItem).doClick();
        JFrameOperator frmAbout = new JFrameOperator("About");  // NOI18N
        Assert.assertNotNull(frmAbout);
        if (PAUSE) JUnitUtil.waitFor(2000);
        new JButtonOperator(frmAbout, Bundle.getMessage("ButtonOK")).doClick();

        // ** File menu **
        jmo = new JMenuOperator(jmbo, Bundle.getMessage("MenuFile"));  // NOI18N
        jpm = jmo.getPopupMenu();

        // MenuNew
        JMenuItem newMenuItem = (JMenuItem) jpm.getComponent(0);
        Assert.assertTrue(newMenuItem.getText().equals(Bundle.getMessage("MenuNew")));  // NOI18N
        Thread btnNew = createModalDialogOperatorThread(Bundle.getMessage("NewConfigTitle"), Bundle.getMessage("ButtonYes"), "btnNew");  // NOI18N
        new JMenuItemOperator(newMenuItem).doClick();
        JUnitUtil.waitFor(() -> {
            return !(btnNew.isAlive());
        }, "btnNew finished");  // NOI18N
        if (PAUSE) JUnitUtil.waitFor(2000);

        // MenuImport -- Reply No, the actual test is in ImportExternalDataTest
        JMenuItem importMenuItem = (JMenuItem) jpm.getComponent(1);
        Assert.assertTrue(importMenuItem.getText().equals(Bundle.getMessage("MenuImport")));  // NOI18N
        importMenuItem.setEnabled(true);
        Thread btnImport = createModalDialogOperatorThread(Bundle.getMessage("ImportTitle"), Bundle.getMessage("ButtonNo"), "btnImport");  // NOI18N
        new JMenuItemOperator(importMenuItem).doClick();
        JUnitUtil.waitFor(() -> {
            return !(btnImport.isAlive());
        }, "btnImport finished");  // NOI18N
        if (PAUSE) JUnitUtil.waitFor(2000);
    }

    void frameButtonTests() {
        JListOperator jlo = new JListOperator(_jfo);

        // ButtonAdd
        JButtonOperator jbAdd = new JButtonOperator(_jfo, Bundle.getMessage("ButtonAdd"));
        jbAdd.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        JFrameOperator frmAdd = new JFrameOperator("Add new Switch and Signal etc. #'s");  // NOI18N
        Assert.assertNotNull(frmAdd);
        if (PAUSE) JUnitUtil.waitFor(2000);
        new JButtonOperator(frmAdd, Bundle.getMessage("ButtonSaveClose")).doClick();
        if (PAUSE) JUnitUtil.waitFor(2000);

        // ButtonMoveUp
        jlo.clickOnItem(2, 1);
        JButtonOperator jbMoveUp = new JButtonOperator(_jfo, Bundle.getMessage("ButtonMoveUp"));
        jbMoveUp.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        if (PAUSE) JUnitUtil.waitFor(2000);

        // ButtonMoveDown
        jlo.clickOnItem(1, 1);
        JButtonOperator jbMoveDown = new JButtonOperator(_jfo, Bundle.getMessage("ButtonMoveDown"));
        jbMoveDown.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        if (PAUSE) JUnitUtil.waitFor(2000);

        // ButtonDelete
        jlo.clickOnItem(2, 1);
        JButtonOperator jbDelete = new JButtonOperator(_jfo, Bundle.getMessage("ButtonDelete"));
        jbDelete.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        if (PAUSE) JUnitUtil.waitFor(2000);

        // ButtonChange
        jlo.clickOnItem(0, 1);
        JButtonOperator jbChange = new JButtonOperator(_jfo, Bundle.getMessage("ButtonChange"));
        jbChange.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        JFrameOperator frmChange = new JFrameOperator("Modify Switch and Signal etc. #'s");  // NOI18N
        Assert.assertNotNull(frmChange);
        if (PAUSE) JUnitUtil.waitFor(2000);
        new JButtonOperator(frmChange, Bundle.getMessage("ButtonSaveClose")).doClick();

        // ButtonXmlFiles -- No GUI object
        JButtonOperator jbXMLFiles = new JButtonOperator(_jfo, Bundle.getMessage("ButtonXmlFiles"));
        jbXMLFiles.doClick();
    }

    void editTests() {
        JListOperator jlo = new JListOperator(_jfo);

        // Code button
        jlo.clickOnItem(0, 1);
        JButtonOperator jbCB = new JButtonOperator(_jfo, Bundle.getMessage("ButtonEdit"), 0);
        jbCB.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        JFrameOperator frmCB = new JFrameOperator(Bundle.getMessage("TitleDlgCB"));  // NOI18N
        Assert.assertNotNull(frmCB);
        new JButtonOperator(frmCB, Bundle.getMessage("ButtonReapply")).doClick();
        if (PAUSE) JUnitUtil.waitFor(2000);
        new JButtonOperator(frmCB, Bundle.getMessage("ButtonSaveClose")).doClick();

        // SIDI button
        jlo.clickOnItem(0, 1);
        JButtonOperator jbSIDI = new JButtonOperator(_jfo, Bundle.getMessage("ButtonEdit"), 1);
        jbSIDI.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        JFrameOperator frmSIDI = new JFrameOperator(Bundle.getMessage("TitleSIDI"));  // NOI18N
        Assert.assertNotNull(frmSIDI);
        new JButtonOperator(frmSIDI, Bundle.getMessage("ButtonReapply")).doClick();
        new JButtonOperator(frmSIDI, Bundle.getMessage("ButtonSIDIBoth")).doClick();
        if (PAUSE) JUnitUtil.waitFor(2000);
        new JButtonOperator(frmSIDI, Bundle.getMessage("ButtonSaveClose")).doClick();

        // SIDL button
        jlo.clickOnItem(0, 1);
        JButtonOperator jbSIDL = new JButtonOperator(_jfo, Bundle.getMessage("ButtonEdit"), 4);
        jbSIDL.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        JFrameOperator frmSIDL = new JFrameOperator(Bundle.getMessage("TitleDlgSIDL"));  // NOI18N
        Assert.assertNotNull(frmSIDL);
        new JButtonOperator(frmSIDL, Bundle.getMessage("ButtonReapply")).doClick();
        if (PAUSE) JUnitUtil.waitFor(2000);
        new JButtonOperator(frmSIDL, Bundle.getMessage("ButtonSaveClose")).doClick();

        // SWDI button
        jlo.clickOnItem(0, 1);
        JButtonOperator jbSWDI = new JButtonOperator(_jfo, Bundle.getMessage("ButtonEdit"), 7);
        jbSWDI.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        JFrameOperator frmSWDI = new JFrameOperator(Bundle.getMessage("TitleSWDI"));  // NOI18N
        Assert.assertNotNull(frmSWDI);
        new JButtonOperator(frmSWDI, Bundle.getMessage("ButtonReapply")).doClick();
        if (PAUSE) JUnitUtil.waitFor(2000);
        new JButtonOperator(frmSWDI, Bundle.getMessage("ButtonSaveClose")).doClick();

        // SWDL button
        jlo.clickOnItem(0, 1);
        JButtonOperator jbSWDL = new JButtonOperator(_jfo, Bundle.getMessage("ButtonEdit"), 8);
        jbSWDL.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        JFrameOperator frmSWDL = new JFrameOperator(Bundle.getMessage("TitleDlgSWDL"));  // NOI18N
        Assert.assertNotNull(frmSWDL);
        new JButtonOperator(frmSWDL, Bundle.getMessage("ButtonReapply")).doClick();
        if (PAUSE) JUnitUtil.waitFor(2000);
        new JButtonOperator(frmSWDL, Bundle.getMessage("ButtonSaveClose")).doClick();

        // CO button
        jlo.clickOnItem(0, 1);
        JButtonOperator jbCO = new JButtonOperator(_jfo, Bundle.getMessage("ButtonEdit"), 2);
        jbCO.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        JFrameOperator frmCO = new JFrameOperator(Bundle.getMessage("TitleDlgCO"));  // NOI18N
        Assert.assertNotNull(frmCO);
        if (PAUSE) JUnitUtil.waitFor(2000);
        new JButtonOperator(frmCO, Bundle.getMessage("ButtonReapply")).doClick();
        doCallOnRules(frmCO);
        new JButtonOperator(frmCO, Bundle.getMessage("ButtonSaveClose")).doClick();

        // TUL button
        jlo.clickOnItem(0, 1);
        JButtonOperator jbTUL = new JButtonOperator(_jfo, Bundle.getMessage("ButtonEdit"), 5);
        jbTUL.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        JFrameOperator frmTUL = new JFrameOperator(Bundle.getMessage("TitleDlgTUL"));  // NOI18N
        Assert.assertNotNull(frmTUL);
        new JButtonOperator(frmTUL, Bundle.getMessage("ButtonReapply")).doClick();
        if (PAUSE) JUnitUtil.waitFor(2000);
        new JButtonOperator(frmTUL, Bundle.getMessage("ButtonSaveClose")).doClick();

        // IL button
        jlo.clickOnItem(0, 1);
        JButtonOperator jbIL = new JButtonOperator(_jfo, Bundle.getMessage("ButtonEdit"), 6);
        jbIL.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        JFrameOperator frmIL = new JFrameOperator(Bundle.getMessage("TitleDlgIL"));  // NOI18N
        Assert.assertNotNull(frmIL);
        new JButtonOperator(frmIL, Bundle.getMessage("ButtonDlgILCompact")).doClick();
        if (PAUSE) JUnitUtil.waitFor(2000);
        new JButtonOperator(frmIL, Bundle.getMessage("ButtonSaveClose")).doClick();

        // TRL button -- Special handling required since the TRL frame starts the Rules frame.
        // This test should also be the last one.
        jlo.clickOnItem(0, 1);

        // Open the TRL frame
        JButtonOperator jbTRL = new JButtonOperator(_jfo, Bundle.getMessage("ButtonEdit"), 3);
        jbTRL.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        JFrameOperator frmTRL = new JFrameOperator(Bundle.getMessage("TitleDlgTRL"));  // NOI18N
        Assert.assertNotNull(frmTRL);
        if (PAUSE) JUnitUtil.waitFor(2000);

        // Do the automatic TRL generation
        JButtonOperator jbAutoGen = new JButtonOperator(frmTRL, Bundle.getMessage("LabelDlgTRLAutoGenerate"), 0);
        jbAutoGen.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }

        // Open the right rules frame and do the detail checks
        JButtonOperator jbRules = new JButtonOperator(frmTRL, Bundle.getMessage("ButtonEdit"), 0);
        jbRules.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        JFrameOperator frmRules = new JFrameOperator("Edit Right traffic locking rules");  // NOI18N
        Assert.assertNotNull(frmRules);
        doTrlRules(frmRules);
        if (PAUSE) JUnitUtil.waitFor(2000);

        // Save and closed the right rules
        new JButtonOperator(frmRules, Bundle.getMessage("ButtonSaveClose")).doClick();


        // Open the left rules frame
        jbRules = new JButtonOperator(frmTRL, Bundle.getMessage("ButtonEdit"), 1);
        jbRules.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        JFrameOperator frmRulesLeft = new JFrameOperator("Edit Left traffic locking rules");  // NOI18N
        Assert.assertNotNull(frmRulesLeft);
        if (PAUSE) JUnitUtil.waitFor(2000);
        frmRulesLeft.close();

        // Close the TRL summary
        new JButtonOperator(frmTRL, Bundle.getMessage("ButtonOK")).doClick();
    }

    Thread createModalDialogOperatorThread(String dialogTitle, String buttonText, String threadName) {
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(dialogTitle);
            if (PAUSE) JUnitUtil.waitFor(2000);
            JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
            jbo.pushNoBlock();
        });
        t.setName(dialogTitle + " Close Dialog Thread: " + threadName);  // NOI18N
        t.start();
        return t;
    }

    void toggleCheckBoxes() {
        JListOperator jlo = new JListOperator(_jfo);
        jlo.clickOnItem(0, 1);

        for (int index = 0; index < 8; index++) {
            JCheckBoxOperator xbox = new JCheckBoxOperator(_jfo, index);
            xbox.doClick();
            xbox.doClick();
        }
    }

    void doCallOnRules(JFrameOperator rules) {
        JListOperator jloRules = new JListOperator(rules);

        // add new, signal required, block required
        // Add button
        JButtonOperator jbAdd = new JButtonOperator(rules, Bundle.getMessage("ButtonAddNew"));
        jbAdd.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        if (PAUSE) JUnitUtil.waitFor(2000);

        // Select signal mast
        JComboBoxOperator jcbo = new JComboBoxOperator(rules, 1);
        jcbo.setSelectedItem("SM-Alpha-Left-B");

        // Select block
        jcbo = new JComboBoxOperator(rules, 11);
        jcbo.setSelectedItem("B-Right-Approach");

        // Add this...; reply yes to permissive warning
        JButtonOperator jbAddToEnd = new JButtonOperator(rules, Bundle.getMessage("TextDlgCOAddInstructions"));
        Thread btnAddRow = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonYes"), "btnAddRow");  // NOI18N
        jbAddToEnd.doClick();
        JUnitUtil.waitFor(() -> {
            return !(btnAddRow.isAlive());
        }, "btnAddRow finished");  // NOI18N

        // Edit row
        jloRules.clickOnItem(0, 1);
        JButtonOperator jbEdit = new JButtonOperator(rules, Bundle.getMessage("ButtonEditBelow"));
        jbEdit.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        if (PAUSE) JUnitUtil.waitFor(2000);

        // Update...
        JButtonOperator jbUpdate = new JButtonOperator(rules, Bundle.getMessage("TextDlgCOUpdateInstructions"));
        jbUpdate.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        if (PAUSE) JUnitUtil.waitFor(2000);

        jloRules.clickOnItem(2, 1);
        jbEdit = new JButtonOperator(rules, Bundle.getMessage("ButtonEditBelow"));
        jbEdit.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        if (PAUSE) JUnitUtil.waitFor(2000);

        // Cancel...
        JButtonOperator jbCancel = new JButtonOperator(rules, Bundle.getMessage("ButtonCancel"));
        jbCancel.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        if (PAUSE) JUnitUtil.waitFor(2000);

        jloRules.clickOnItem(2, 1);
        JButtonOperator jbDelete = new JButtonOperator(rules, Bundle.getMessage("ButtonDelete"));
        jbDelete.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        if (PAUSE) JUnitUtil.waitFor(5000);
    }

    void doTrlRules(JFrameOperator rules) {
        JListOperator jloRules = new JListOperator(rules);

        // Add button
        JButtonOperator jbAdd = new JButtonOperator(rules, Bundle.getMessage("ButtonAddNew"));
        jbAdd.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        if (PAUSE) JUnitUtil.waitFor(2000);

        // Add this...
        JButtonOperator jbAddToEnd = new JButtonOperator(rules, Bundle.getMessage("TextDlgTRLRulesAddThis"));
        jbAddToEnd.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        if (PAUSE) JUnitUtil.waitFor(2000);

        // duplicate row
        jloRules.clickOnItem(0, 1);
        JButtonOperator jbDup = new JButtonOperator(rules, Bundle.getMessage("ButtonDlgTRLRules"));
        jbDup.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        if (PAUSE) JUnitUtil.waitFor(2000);

        // edit row
        jloRules.clickOnItem(0, 1);
        JButtonOperator jbEdit = new JButtonOperator(rules, Bundle.getMessage("ButtonEditBelow"));
        jbEdit.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        if (PAUSE) JUnitUtil.waitFor(2000);

        // Update...
        JButtonOperator jbUpdate = new JButtonOperator(rules, Bundle.getMessage("TextDlgTRLRulesUpdateThis"));
        jbUpdate.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        if (PAUSE) JUnitUtil.waitFor(2000);

        jloRules.clickOnItem(2, 1);
        jbEdit = new JButtonOperator(rules, Bundle.getMessage("ButtonEditBelow"));
        jbEdit.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        if (PAUSE) JUnitUtil.waitFor(2000);

        // Cancel...
        JButtonOperator jbCancel = new JButtonOperator(rules, Bundle.getMessage("ButtonCancel"));
        jbCancel.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        if (PAUSE) JUnitUtil.waitFor(2000);

        jloRules.clickOnItem(2, 1);
        JButtonOperator jbDelete = new JButtonOperator(rules, Bundle.getMessage("ButtonDelete"));
        jbDelete.doClick();
        if (DELAY > 0) {
            new EventTool().waitNoEvent(DELAY);
        }
        if (PAUSE) JUnitUtil.waitFor(5000);
    }

    @Test
    public void testMakePanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertThrows(IllegalArgumentException.class, () -> new CtcEditorAction().makePanel());
    }

    @BeforeEach
    public void setUp(@TempDir File folder) throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new NullProfile(folder));
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initRouteManager();
        JUnitUtil.initDefaultSignalMastManager();
        JUnitUtil.initSignalMastLogicManager();
        JUnitUtil.initLayoutBlockManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CtcEditorSignalMastsTest.class);
}
