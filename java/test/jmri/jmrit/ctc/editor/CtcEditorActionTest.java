package jmri.jmrit.ctc.editor;

import java.awt.GraphicsEnvironment;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import jmri.util.JUnitUtil;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.netbeans.jemmy.operators.*;

/**
 * Tests for the CtcEditorAction Class
 * @author Dave Sand Copyright (C) 2019
 */
public class CtcEditorActionTest {
    JFrameOperator _jfo = null;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Rule
    public org.junit.rules.TemporaryFolder folder = new org.junit.rules.TemporaryFolder();

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
    }

    void menuTests() {
        JMenuBarOperator jmbo = new JMenuBarOperator(_jfo); // there's only one menubar
        JMenuOperator jmo;
        JPopupMenu jpm;

        // ** Edit menu **
        jmo = new JMenuOperator(jmbo, Bundle.getMessage("MenuEdit"));  // NOI18N
        jpm = jmo.getPopupMenu();

        // MenuFind
        JMenuItem findMenuItem = (JMenuItem)jpm.getComponent(0);
        Assert.assertTrue(findMenuItem.getText().equals(Bundle.getMessage("MenuFind")));  // NOI18N
        new JMenuItemOperator(findMenuItem).doClick();
        JFrameOperator frmFind = new JFrameOperator(Bundle.getMessage("TitleDlgFind"));  // NOI18N
        Assert.assertNotNull(frmFind);
        new JButtonOperator(frmFind, Bundle.getMessage("ButtonDone")).pushNoBlock();

        // MenuFix
        JMenuItem fixMenuItem = (JMenuItem)jpm.getComponent(1);
        Assert.assertTrue(fixMenuItem.getText().equals(Bundle.getMessage("MenuFix")));  // NOI18N
        new JMenuItemOperator(fixMenuItem).doClick();
        JFrameOperator frmFix = new JFrameOperator(Bundle.getMessage("TitleDlgFix"));  // NOI18N
        Assert.assertNotNull(frmFix);
        new JButtonOperator(frmFix, Bundle.getMessage("ButtonCancel")).pushNoBlock();

        // ** Configure menu **
        jmo = new JMenuOperator(jmbo, Bundle.getMessage("MenuConfigure"));  // NOI18N
        jpm = jmo.getPopupMenu();

        // MenuDebugging
        JMenuItem debugMenuItem = (JMenuItem)jpm.getComponent(0);
        Assert.assertTrue(debugMenuItem.getText().equals(Bundle.getMessage("MenuDebugging")));  // NOI18N
        new JMenuItemOperator(debugMenuItem).doClick();
        JFrameOperator frmDebug = new JFrameOperator(Bundle.getMessage("TitleDlgDeb"));  // NOI18N
        Assert.assertNotNull(frmDebug);
        new JButtonOperator(frmDebug, Bundle.getMessage("ButtonSaveClose")).pushNoBlock();

        // MenuDefaults
        JMenuItem defaultMenuItem = (JMenuItem)jpm.getComponent(1);
        Assert.assertTrue(defaultMenuItem.getText().equals(Bundle.getMessage("MenuDefaults")));  // NOI18N
        new JMenuItemOperator(defaultMenuItem).doClick();
        JFrameOperator frmDefaults = new JFrameOperator(Bundle.getMessage("TitleDlgDef"));  // NOI18N
        Assert.assertNotNull(frmDefaults);
        new JButtonOperator(frmDefaults, Bundle.getMessage("ButtonSaveClose")).pushNoBlock();

        // MenuFleeting
        JMenuItem fleetMenuItem = (JMenuItem)jpm.getComponent(2);
        Assert.assertTrue(fleetMenuItem.getText().equals(Bundle.getMessage("MenuFleeting")));  // NOI18N
        new JMenuItemOperator(fleetMenuItem).doClick();
        JFrameOperator frmFleeting = new JFrameOperator(Bundle.getMessage("TitleDlgFleet"));  // NOI18N
        Assert.assertNotNull(frmFleeting);
        new JButtonOperator(frmFleeting, Bundle.getMessage("ButtonSaveClose")).pushNoBlock();

        // MenuPatterns
        JMenuItem patternMenuItem = (JMenuItem)jpm.getComponent(3);
        Assert.assertTrue(patternMenuItem.getText().equals(Bundle.getMessage("MenuPatterns")));  // NOI18N
        new JMenuItemOperator(patternMenuItem).doClick();
        JFrameOperator frmPatterns = new JFrameOperator(Bundle.getMessage("TItleDlgPat"));  // NOI18N
        Assert.assertNotNull(frmPatterns);
        new JButtonOperator(frmPatterns, Bundle.getMessage("ButtonSaveClose")).pushNoBlock();

        // MenuDesign
        JMenuItem designMenuItem = (JMenuItem)jpm.getComponent(4);
        Assert.assertTrue(designMenuItem.getText().equals(Bundle.getMessage("MenuDesign")));  // NOI18N
        new JMenuItemOperator(designMenuItem).doClick();
        JFrameOperator frmGUI = new JFrameOperator(Bundle.getMessage("TitleDlgGUI"));  // NOI18N
        Assert.assertNotNull(frmGUI);
        new JButtonOperator(frmGUI, Bundle.getMessage("ButtonSaveClose")).pushNoBlock();

        // ** About menu **
        jmo = new JMenuOperator(jmbo, Bundle.getMessage("MenuAbout"));  // NOI18N
        jpm = jmo.getPopupMenu();

        // MenuAbout
        JMenuItem aboutMenuItem = (JMenuItem)jpm.getComponent(0);
        Assert.assertTrue(aboutMenuItem.getText().equals(Bundle.getMessage("MenuAbout")));  // NOI18N
        new JMenuItemOperator(aboutMenuItem).doClick();
        JFrameOperator frmAbout = new JFrameOperator("About");  // NOI18N
        Assert.assertNotNull(frmAbout);
        new JButtonOperator(frmAbout, Bundle.getMessage("ButtonOK")).pushNoBlock();
    }

    void frameButtonTests() {
        JListOperator jlo = new JListOperator(_jfo);

        // ButtonChange
        jlo.clickOnItem(0, 1);
        JButtonOperator jbChange = new JButtonOperator(_jfo, Bundle.getMessage("ButtonChange"));
        jbChange.doClick();
        JFrameOperator frmChange = new JFrameOperator("Modify switch and signal");  // NOI18N
        Assert.assertNotNull(frmChange);
        new JButtonOperator(frmChange, Bundle.getMessage("ButtonSaveClose")).pushNoBlock();

        // ButtonCheck -- Not a frame, use dialog mode
        Thread btnCheck = createModalDialogOperatorThread("Info", Bundle.getMessage("ButtonNo"), "btnCheck");  // NOI18N
        JButtonOperator jbCheck = new JButtonOperator(_jfo, Bundle.getMessage("ButtonCheck"));
        jbCheck.doClick();
        JUnitUtil.waitFor(()->{return !(btnCheck.isAlive());}, "btnCheck finished");  // NOI18N

        // ButtonReapplyItem -- Not a frame, use dialog mode
        jlo.clickOnItem(0, 1);
        Thread btnApply = createModalDialogOperatorThread("Warning", Bundle.getMessage("ButtonYes"), "btnApply");  // NOI18N
        JButtonOperator jbApply = new JButtonOperator(_jfo, Bundle.getMessage("ButtonReapplyItem"));
        jbApply.doClick();
        JUnitUtil.waitFor(()->{return !(btnApply.isAlive());}, "btnApply finished");  // NOI18N

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
        JFrameOperator frmCB = new JFrameOperator(Bundle.getMessage("TitleDlgCB"));  // NOI18N
        Assert.assertNotNull(frmCB);
        new JButtonOperator(frmCB, Bundle.getMessage("ButtonSaveClose")).pushNoBlock();

        // SIDI button
        jlo.clickOnItem(0, 1);
        JButtonOperator jbSIDI = new JButtonOperator(_jfo, Bundle.getMessage("ButtonEdit"), 1);
        jbSIDI.doClick();
        JFrameOperator frmSIDI = new JFrameOperator(Bundle.getMessage("TitleSIDI"));  // NOI18N
        Assert.assertNotNull(frmSIDI);
        new JButtonOperator(frmSIDI, Bundle.getMessage("ButtonSaveClose")).pushNoBlock();

        // SIDL button
        jlo.clickOnItem(0, 1);
        JButtonOperator jbSIDL = new JButtonOperator(_jfo, Bundle.getMessage("ButtonEdit"), 4);
        jbSIDL.doClick();
        JFrameOperator frmSIDL = new JFrameOperator(Bundle.getMessage("TitleDlgSIDL"));  // NOI18N
        Assert.assertNotNull(frmSIDL);
        new JButtonOperator(frmSIDL, Bundle.getMessage("ButtonSaveClose")).pushNoBlock();

        // SWDI button
        jlo.clickOnItem(0, 1);
        JButtonOperator jbSWDI = new JButtonOperator(_jfo, Bundle.getMessage("ButtonEdit"), 7);
        jbSWDI.doClick();
        JFrameOperator frmSWDI = new JFrameOperator(Bundle.getMessage("TitleSWDI"));  // NOI18N
        Assert.assertNotNull(frmSWDI);
        new JButtonOperator(frmSWDI, Bundle.getMessage("ButtonSaveClose")).pushNoBlock();

        // SWDL button
        jlo.clickOnItem(0, 1);
        JButtonOperator jbSWDL = new JButtonOperator(_jfo, Bundle.getMessage("ButtonEdit"), 8);
        jbSWDL.doClick();
        JFrameOperator frmSWDL = new JFrameOperator(Bundle.getMessage("TitleDlgSWDL"));  // NOI18N
        Assert.assertNotNull(frmSWDL);
        new JButtonOperator(frmSWDL, Bundle.getMessage("ButtonSaveClose")).pushNoBlock();

        // CO button
        jlo.clickOnItem(0, 1);
        JButtonOperator jbCO = new JButtonOperator(_jfo, Bundle.getMessage("ButtonEdit"), 2);
        jbCO.doClick();
        JFrameOperator frmCO = new JFrameOperator(Bundle.getMessage("TitleDlgCO"));  // NOI18N
        Assert.assertNotNull(frmCO);
        new JButtonOperator(frmCO, Bundle.getMessage("ButtonSaveClose")).pushNoBlock();

        // TUL button
        jlo.clickOnItem(0, 1);
        JButtonOperator jbTUL = new JButtonOperator(_jfo, Bundle.getMessage("ButtonEdit"), 5);
        jbTUL.doClick();
        JFrameOperator frmTUL = new JFrameOperator(Bundle.getMessage("TitleDlgTUL"));  // NOI18N
        Assert.assertNotNull(frmTUL);
        new JButtonOperator(frmTUL, Bundle.getMessage("ButtonSaveClose")).pushNoBlock();

        // IL button
        jlo.clickOnItem(0, 1);
        JButtonOperator jbIL = new JButtonOperator(_jfo, Bundle.getMessage("ButtonEdit"), 6);
        jbIL.doClick();
        JFrameOperator frmIL = new JFrameOperator(Bundle.getMessage("TitleDlgIL"));  // NOI18N
        Assert.assertNotNull(frmIL);
        new JButtonOperator(frmIL, Bundle.getMessage("ButtonSaveClose")).pushNoBlock();

        // TRL button -- Special handling required since the TRL frame starts the Rules frame.
        // This test should also be the last one.
        jlo.clickOnItem(0, 1);

        // Open the TRL frame
        JButtonOperator jbTRL = new JButtonOperator(_jfo, Bundle.getMessage("ButtonEdit"), 3);
        jbTRL.doClick();
        JFrameOperator frmTRL = new JFrameOperator(Bundle.getMessage("TitleDlgTRL"));  // NOI18N
        Assert.assertNotNull(frmTRL);

        // Open the Rules frame
        JButtonOperator jbRules = new JButtonOperator(frmTRL, Bundle.getMessage("ButtonEdit"), 0);
        jbRules.doClick();
        JFrameOperator frmRules = new JFrameOperator("Edit Right traffic locking rules");  // NOI18N
        Assert.assertNotNull(frmRules);

        // Close them in reverse order
        new JButtonOperator(frmRules, Bundle.getMessage("ButtonSaveClose")).pushNoBlock();
        new JButtonOperator(frmTRL, Bundle.getMessage("ButtonOK")).pushNoBlock();
    }

    Thread createModalDialogOperatorThread(String dialogTitle, String buttonText, String threadName) {
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(dialogTitle);
            JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
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

        try {
            JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder.newFolder(jmri.profile.Profile.PROFILE)));
        } catch(java.io.IOException ioe){
            Assert.fail("failed to setup profile for test");
        }

        jmri.jmrit.ctc.setup.CreateTestObjects.createTestObjects();
        jmri.jmrit.ctc.setup.CreateTestObjects.createTestFiles();
    }

    @After
    public void tearDown() {
        // use reflection to reset the static file location.
        try {
            Class<?> c = jmri.jmrit.ctc.CTCFiles.class;
            java.lang.reflect.Field f = c.getDeclaredField("fileLocation");
            f.setAccessible(true);
            f.set(new String(), null);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException x) {
            Assert.fail("Failed to reset CTC static fileLocation " + x);
        }

        jmri.util.JUnitUtil.tearDown();
    }

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CtcEditorActionTest.class);
}