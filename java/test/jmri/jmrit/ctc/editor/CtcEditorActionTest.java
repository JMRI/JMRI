package jmri.jmrit.ctc.editor;

import java.awt.GraphicsEnvironment;
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

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void testCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new CtcEditorAction();
// new EventTool().waitNoEvent(1000);
    }

    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new CtcEditorAction().actionPerformed(null);
        new CtcEditorAction().actionPerformed(null);

        JFrameOperator _jfo = new JFrameOperator(0);
        Assert.assertNotNull(_jfo);
        JListOperator jlo = new JListOperator(_jfo);

        // Code button
        jlo.clickOnItem(0, 1);
        Thread editCB = createModalDialogOperatorThread(Bundle.getMessage("TitleDlgCB"), Bundle.getMessage("ButtonSaveClose"), "editCB");  // NOI18N
        JButtonOperator jbCB = new JButtonOperator(_jfo, "Edit", 0);
        jbCB.doClick();
        JUnitUtil.waitFor(()->{return !(editCB.isAlive());}, "editCB finished");  // NOI18N

        // SIDI button
        jlo.clickOnItem(0, 1);
        Thread editSIDI = createModalDialogOperatorThread(Bundle.getMessage("TitleSIDI"), Bundle.getMessage("ButtonSaveClose"), "editSIDI");  // NOI18N
        JButtonOperator jbSIDI = new JButtonOperator(_jfo, "Edit", 1);
        jbSIDI.doClick();
        JUnitUtil.waitFor(()->{return !(editSIDI.isAlive());}, "editSIDI finished");  // NOI18N

        // SIDL button
        jlo.clickOnItem(0, 1);
        Thread editSIDL = createModalDialogOperatorThread(Bundle.getMessage("TitleDlgSIDL"), Bundle.getMessage("ButtonSaveClose"), "editSIDL");  // NOI18N
        JButtonOperator jbSIDL = new JButtonOperator(_jfo, "Edit", 4);
        jbSIDL.doClick();
        JUnitUtil.waitFor(()->{return !(editSIDL.isAlive());}, "editSIDL finished");  // NOI18N

        // SWDI button
        jlo.clickOnItem(0, 1);
        Thread editSWDI= createModalDialogOperatorThread(Bundle.getMessage("TitleSWDI"), Bundle.getMessage("ButtonSaveClose"), "editSWDI");  // NOI18N
        JButtonOperator jbSWDI = new JButtonOperator(_jfo, "Edit", 7);
        jbSWDI.doClick();
        JUnitUtil.waitFor(()->{return !(editSWDI.isAlive());}, "editSWDI finished");  // NOI18N

        // SWDL button
        jlo.clickOnItem(0, 1);
        Thread editSWDL= createModalDialogOperatorThread(Bundle.getMessage("TitleDlgSWDL"), Bundle.getMessage("ButtonSaveClose"), "editSWDL");  // NOI18N
        JButtonOperator jbSWDL = new JButtonOperator(_jfo, "Edit", 8);
        jbSWDL.doClick();
        JUnitUtil.waitFor(()->{return !(editSWDL.isAlive());}, "editSWDL finished");  // NOI18N

        // CO button
        jlo.clickOnItem(0, 1);
        Thread editCO= createModalDialogOperatorThread(Bundle.getMessage("TitleDlgCO"), Bundle.getMessage("ButtonSaveClose"), "editCO");  // NOI18N
        JButtonOperator jbCO = new JButtonOperator(_jfo, "Edit", 2);
        jbCO.doClick();
        JUnitUtil.waitFor(()->{return !(editCO.isAlive());}, "editCO finished");  // NOI18N

        // TUL button
        jlo.clickOnItem(0, 1);
        Thread editTUL= createModalDialogOperatorThread(Bundle.getMessage("TitleDlgTUL"), Bundle.getMessage("ButtonSaveClose"), "editTUL");  // NOI18N
        JButtonOperator jbTUL = new JButtonOperator(_jfo, "Edit", 5);
        jbTUL.doClick();
        JUnitUtil.waitFor(()->{return !(editTUL.isAlive());}, "editTUL finished");  // NOI18N

        // TRL button - requires special handling.
        // First a left/right Edit has to be selected which gets the rules dialog.
        // After exiting the rules dialog, the initial dialog returns and needs OK.
        // 1 - TRL, select edit
        // 2 - Rules, select save close, may need focus
        // 3 - TRL, select OK

//         jlo.clickOnItem(0, 1);
//         JButtonOperator jbTRL1 = new JButtonOperator(_jfo, "Edit", 3);
//         Thread editTRL1 = createModalDialogOperatorThread(Bundle.getMessage("TitleDlgTRL"), "Edit", "editTRL1");  // NOI18N
//         Thread editRules = createModalDialogOperatorThread(Bundle.getMessage("TitleDlgTRLRules"), Bundle.getMessage("ButtonSaveClose"), "editRules");  // NOI18N
//         jbTRL1.doClick();
// //         JUnitUtil.waitFor(()->{return !(editTRL1.isAlive());}, "editTRL 1 finished");  // NOI18N
//         JUnitUtil.waitFor(()->{return !(editRules.isAlive());}, "editRules finished");  // NOI18N


//         Thread editRules = createModalDialogOperatorThread(Bundle.getMessage("TitleDlgTUL"), Bundle.getMessage("ButtonSaveClose"), "editRules");  // NOI18N
//         jbTRL.doClick();
//         JUnitUtil.waitFor(()->{return !(editTRL.isAlive());}, "editTRL finished");  // NOI18N
//         JButtonOperator jbTRL = new JButtonOperator(_jfo, "Edit", 3);
//         jbTRL.doClick();
//         JUnitUtil.waitFor(()->{return !(editTRL.isAlive());}, "editTRL finished");  // NOI18N





// 2 is callon
// 3 is traffic locking
// 5 is turnout locking
// 6 is ??? IL

// new EventTool().waitNoEvent(5000);

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
// new EventTool().waitNoEvent(1000);
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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CtcEditorActionTest.class);
}