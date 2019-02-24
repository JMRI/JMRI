package jmri.jmrit.ctc.editor;

import java.awt.GraphicsEnvironment;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import jmri.util.JUnitUtil;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.*;

/**
 * Tests for the CtcEditorAction Class
 * @author Dave Sand Copyright (C) 2019
 */
public class CtcEditorActionTest {
    JFrameOperator _jfo = null;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void testCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new CtcEditorAction();
    }

    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new CtcEditorAction().actionPerformed(null);
        new CtcEditorAction().actionPerformed(null);

        _jfo = new JFrameOperator(0);
        Assert.assertNotNull(_jfo);
        menuTests();
        frameButtonTests();
        editTests();
// new EventTool().waitNoEvent(5000);
    }

    void menuTests() {
        JMenuBarOperator jmbo = new JMenuBarOperator(_jfo); // there's only one menubar
        JMenuOperator jmo;
        JPopupMenu jpm;

        // ** Edit menu **
        jmo = new JMenuOperator(jmbo, Bundle.getMessage("MenuEdit"));  // NOI18N
        jpm = jmo.getPopupMenu();

        // MenuFind
        Thread menuFind = createModalDialogOperatorThread(Bundle.getMessage("TitleDlgFind"), Bundle.getMessage("ButtonDone"), "menuFind");  // NOI18N
        JMenuItem findMenuItem = (JMenuItem)jpm.getComponent(0);
        Assert.assertTrue(findMenuItem.getText().equals(Bundle.getMessage("MenuFind")));  // NOI18N
        new JMenuItemOperator(findMenuItem).doClick();
        JUnitUtil.waitFor(()->{return !(menuFind.isAlive());}, "menuFind finished");

        // MenuFix
        Thread menuFix = createModalDialogOperatorThread(Bundle.getMessage("TitleDlgFix"), Bundle.getMessage("ButtonCancel"), "menuFix");  // NOI18N
        JMenuItem fixMenuItem = (JMenuItem)jpm.getComponent(1);
        Assert.assertTrue(fixMenuItem.getText().equals(Bundle.getMessage("MenuFix")));  // NOI18N
        new JMenuItemOperator(fixMenuItem).doClick();
        JUnitUtil.waitFor(()->{return !(menuFix.isAlive());}, "menuFix finished");

        // ** Configure menu **
        jmo = new JMenuOperator(jmbo, Bundle.getMessage("MenuConfigure"));  // NOI18N
        jpm = jmo.getPopupMenu();

        // MenuDebugging
        Thread menuDebug = createModalDialogOperatorThread(Bundle.getMessage("TitleDlgDeb"), Bundle.getMessage("ButtonSaveClose"), "menuDebug");  // NOI18N
        JMenuItem debugMenuItem = (JMenuItem)jpm.getComponent(0);
        Assert.assertTrue(debugMenuItem.getText().equals(Bundle.getMessage("MenuDebugging")));  // NOI18N
        new JMenuItemOperator(debugMenuItem).doClick();
        JUnitUtil.waitFor(()->{return !(menuDebug.isAlive());}, "menuDebug finished");

        // MenuDefaults
        Thread menuDefault = createModalDialogOperatorThread(Bundle.getMessage("TitleDlgDef"), Bundle.getMessage("ButtonSaveClose"), "menuDefault");  // NOI18N
        JMenuItem defaultMenuItem = (JMenuItem)jpm.getComponent(1);
        Assert.assertTrue(defaultMenuItem.getText().equals(Bundle.getMessage("MenuDefaults")));  // NOI18N
        new JMenuItemOperator(defaultMenuItem).doClick();
        JUnitUtil.waitFor(()->{return !(menuDefault.isAlive());}, "menuDefault finished");

        // MenuFleeting
        Thread menuFleet = createModalDialogOperatorThread(Bundle.getMessage("TitleDlgFleet"), Bundle.getMessage("ButtonSaveClose"), "menuFleet");  // NOI18N
        JMenuItem fleetMenuItem = (JMenuItem)jpm.getComponent(2);
        Assert.assertTrue(fleetMenuItem.getText().equals(Bundle.getMessage("MenuFleeting")));  // NOI18N
        new JMenuItemOperator(fleetMenuItem).doClick();
        JUnitUtil.waitFor(()->{return !(menuFleet.isAlive());}, "menuFleet finished");

        // MenuPatterns
        Thread menuPatterns = createModalDialogOperatorThread(Bundle.getMessage("TItleDlgPat"), Bundle.getMessage("ButtonSaveClose"), "menuPatterns");  // NOI18N
        JMenuItem patternMenuItem = (JMenuItem)jpm.getComponent(3);
        Assert.assertTrue(patternMenuItem.getText().equals(Bundle.getMessage("MenuPatterns")));  // NOI18N
        new JMenuItemOperator(patternMenuItem).doClick();
        JUnitUtil.waitFor(()->{return !(menuPatterns.isAlive());}, "menuPatterns finished");

        // MenuDesign
        Thread menuDesign = createModalDialogOperatorThread(Bundle.getMessage("TitleDlgGUI"), Bundle.getMessage("ButtonSaveClose"), "menuDesign");  // NOI18N
        JMenuItem designMenuItem = (JMenuItem)jpm.getComponent(4);
        Assert.assertTrue(designMenuItem.getText().equals(Bundle.getMessage("MenuDesign")));  // NOI18N
        new JMenuItemOperator(designMenuItem).doClick();
        JUnitUtil.waitFor(()->{return !(menuDesign.isAlive());}, "menuDesign finished");

        // ** About menu **
        jmo = new JMenuOperator(jmbo, Bundle.getMessage("MenuAbout"));  // NOI18N
        jpm = jmo.getPopupMenu();

        // MenuAbout
        Thread menuAbout = createModalDialogOperatorThread("About", Bundle.getMessage("ButtonOK"), "menuAbout");  // NOI18N
        JMenuItem aboutMenuItem = (JMenuItem)jpm.getComponent(0);
        Assert.assertTrue(aboutMenuItem.getText().equals(Bundle.getMessage("MenuAbout")));  // NOI18N
        new JMenuItemOperator(aboutMenuItem).doClick();
        JUnitUtil.waitFor(()->{return !(menuAbout.isAlive());}, "menuAbout finished");
    }

    void frameButtonTests() {
        JListOperator jlo = new JListOperator(_jfo);

        // ButtonChange
        jlo.clickOnItem(0, 1);
        Thread btnChange = createModalDialogOperatorThread("Modify switch and signal", Bundle.getMessage("ButtonSaveClose"), "btnChange");  // NOI18N
        JButtonOperator jbChange = new JButtonOperator(_jfo, Bundle.getMessage("ButtonChange"));
        jbChange.doClick();
        JUnitUtil.waitFor(()->{return !(btnChange.isAlive());}, "btnChange finished");  // NOI18N

        // ButtonCheck
        Thread btnCheck = createModalDialogOperatorThread("Info", Bundle.getMessage("ButtonNo"), "btnCheck");  // NOI18N
        JButtonOperator jbCheck = new JButtonOperator(_jfo, Bundle.getMessage("ButtonCheck"));
        jbCheck.doClick();
        JUnitUtil.waitFor(()->{return !(btnCheck.isAlive());}, "btnCheck finished");  // NOI18N

        // ButtonReapplyItem
        jlo.clickOnItem(0, 1);
        Thread btnApply = createModalDialogOperatorThread("Warning", Bundle.getMessage("ButtonYes"), "btnApply");  // NOI18N
        JButtonOperator jbApply = new JButtonOperator(_jfo, Bundle.getMessage("ButtonReapplyItem"));
        jbApply.doClick();
        JUnitUtil.waitFor(()->{return !(btnApply.isAlive());}, "btnApply finished");  // NOI18N

        // ButtonXmlFiles
        JButtonOperator jbXMLFiles = new JButtonOperator(_jfo, Bundle.getMessage("ButtonXmlFiles"));
        jbXMLFiles.doClick();
    }

    void editTests() {
        JListOperator jlo = new JListOperator(_jfo);

        // Code button
        jlo.clickOnItem(0, 1);
        Thread editCB = createModalDialogOperatorThread(Bundle.getMessage("TitleDlgCB"), Bundle.getMessage("ButtonSaveClose"), "editCB");  // NOI18N
        JButtonOperator jbCB = new JButtonOperator(_jfo, Bundle.getMessage("ButtonEdit"), 0);
        jbCB.doClick();
        JUnitUtil.waitFor(()->{return !(editCB.isAlive());}, "editCB finished");  // NOI18N

        // SIDI button
        jlo.clickOnItem(0, 1);
        Thread editSIDI = createModalDialogOperatorThread(Bundle.getMessage("TitleSIDI"), Bundle.getMessage("ButtonSaveClose"), "editSIDI");  // NOI18N
        JButtonOperator jbSIDI = new JButtonOperator(_jfo, Bundle.getMessage("ButtonEdit"), 1);
        jbSIDI.doClick();
        JUnitUtil.waitFor(()->{return !(editSIDI.isAlive());}, "editSIDI finished");  // NOI18N

        // SIDL button
        jlo.clickOnItem(0, 1);
        Thread editSIDL = createModalDialogOperatorThread(Bundle.getMessage("TitleDlgSIDL"), Bundle.getMessage("ButtonSaveClose"), "editSIDL");  // NOI18N
        JButtonOperator jbSIDL = new JButtonOperator(_jfo, Bundle.getMessage("ButtonEdit"), 4);
        jbSIDL.doClick();
        JUnitUtil.waitFor(()->{return !(editSIDL.isAlive());}, "editSIDL finished");  // NOI18N

        // SWDI button
        jlo.clickOnItem(0, 1);
        Thread editSWDI= createModalDialogOperatorThread(Bundle.getMessage("TitleSWDI"), Bundle.getMessage("ButtonSaveClose"), "editSWDI");  // NOI18N
        JButtonOperator jbSWDI = new JButtonOperator(_jfo, Bundle.getMessage("ButtonEdit"), 7);
        jbSWDI.doClick();
        JUnitUtil.waitFor(()->{return !(editSWDI.isAlive());}, "editSWDI finished");  // NOI18N

        // SWDL button
        jlo.clickOnItem(0, 1);
        Thread editSWDL= createModalDialogOperatorThread(Bundle.getMessage("TitleDlgSWDL"), Bundle.getMessage("ButtonSaveClose"), "editSWDL");  // NOI18N
        JButtonOperator jbSWDL = new JButtonOperator(_jfo, Bundle.getMessage("ButtonEdit"), 8);
        jbSWDL.doClick();
        JUnitUtil.waitFor(()->{return !(editSWDL.isAlive());}, "editSWDL finished");  // NOI18N

        // CO button
        jlo.clickOnItem(0, 1);
        Thread editCO= createModalDialogOperatorThread(Bundle.getMessage("TitleDlgCO"), Bundle.getMessage("ButtonSaveClose"), "editCO");  // NOI18N
        JButtonOperator jbCO = new JButtonOperator(_jfo, Bundle.getMessage("ButtonEdit"), 2);
        jbCO.doClick();
        JUnitUtil.waitFor(()->{return !(editCO.isAlive());}, "editCO finished");  // NOI18N

        // TUL button
        jlo.clickOnItem(0, 1);
        Thread editTUL= createModalDialogOperatorThread(Bundle.getMessage("TitleDlgTUL"), Bundle.getMessage("ButtonSaveClose"), "editTUL");  // NOI18N
        JButtonOperator jbTUL = new JButtonOperator(_jfo, Bundle.getMessage("ButtonEdit"), 5);
        jbTUL.doClick();
        JUnitUtil.waitFor(()->{return !(editTUL.isAlive());}, "editTUL finished");  // NOI18N

        // IL button
        jlo.clickOnItem(0, 1);
        Thread editIL= createModalDialogOperatorThread(Bundle.getMessage("TitleDlgIL"), Bundle.getMessage("ButtonSaveClose"), "editIL");  // NOI18N
        JButtonOperator jbIL = new JButtonOperator(_jfo, Bundle.getMessage("ButtonEdit"), 6);
        jbIL.doClick();
        JUnitUtil.waitFor(()->{return !(editIL.isAlive());}, "editIL finished");  // NOI18N

        // TRL button - requires special handling.
        // First a left/right Edit has to be selected which gets the rules dialog.
        // After exiting the rules dialog, the initial dialog returns and needs OK.
        // 1 - TRL, select edit
        // 2 - Rules, select save close, may need focus
        // 3 - TRL, select OK

//         jlo.clickOnItem(0, 1);
//         JButtonOperator jbTRL = new JButtonOperator(_jfo, "Edit", 3);
//         jbTRL.doClick();
//             JDialogOperator jdo = new JDialogOperator("TitleDlgTRL");
//             JButtonOperator jbo = new JButtonOperator(jdo, Bundle.getMessage("ButtonOK"));
// new EventTool().waitNoEvent(2000);
//             jbo.pushNoBlock();

//         Thread editTRL1 = createModalDialogOperatorThread(Bundle.getMessage("TitleDlgTRL"), "Edit", "editTRL1");  // NOI18N
//         Thread editRules = createModalDialogOperatorThread(Bundle.getMessage("TitleDlgTRLRules"), Bundle.getMessage("ButtonSaveClose"), "editRules");  // NOI18N
// //         JUnitUtil.waitFor(()->{return !(editTRL1.isAlive());}, "editTRL 1 finished");  // NOI18N
//         JUnitUtil.waitFor(()->{return !(editRules.isAlive());}, "editRules finished");  // NOI18N


//         Thread editRules = createModalDialogOperatorThread(Bundle.getMessage("TitleDlgTUL"), Bundle.getMessage("ButtonSaveClose"), "editRules");  // NOI18N
//         jbTRL.doClick();
//         JUnitUtil.waitFor(()->{return !(editTRL.isAlive());}, "editTRL finished");  // NOI18N
//         JButtonOperator jbTRL = new JButtonOperator(_jfo, "Edit", 3);
//         jbTRL.doClick();
//         JUnitUtil.waitFor(()->{return !(editTRL.isAlive());}, "editTRL finished");  // NOI18N



    }

    Thread createModalDialogOperatorThread(String dialogTitle, String buttonText, String threadName) {
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(dialogTitle);
            JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
// new EventTool().waitNoEvent(2000);
            jbo.pushNoBlock();
        });
        t.setName(dialogTitle + " Close Dialog Thread: " + threadName);  // NOI18N
        t.start();
        return t;
    }

    @Test
    public void testMakePanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        thrown.expect(IllegalArgumentException.class);
        new CtcEditorAction().makePanel();
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        jmri.jmrit.ctc.setup.CreateTestObjects.createTestObjects();
        jmri.jmrit.ctc.setup.CreateTestObjects.createTestFiles();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CtcEditorActionTest.class);
}