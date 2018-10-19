package jmri.jmrit.timetable.swing;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.timetable.*;
import jmri.util.JUnitUtil;
import org.junit.*;
import org.netbeans.jemmy.operators.*;


/**
 * Tests for the TimeTableFrame Class
 * @author Dave Sand Copyright (C) 2018
 */
public class TimeTableFrameTest {
    static TimeTableFrame ttf = null;

    @Test
    public void testCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull(ttf);
    }

    @Test
    public void testCreatEmtpy() {
        TimeTableFrame f = new TimeTableFrame();
        Assert.assertNotNull(f);
    }

    @Test
    public void testTree()  throws InterruptedException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrameOperator editFrame = new JFrameOperator(Bundle.getMessage("TitleTimeTable"));  // NOI18N
        Assert.assertNotNull(editFrame);
        JTreeOperator jto = new JTreeOperator(editFrame);
        JTextFieldOperator textField;
        Assert.assertNotNull(jto);

        // Test move buttons
        jto.collapseRow(0);
        jto.expandRow(0);
        jto.expandRow(3);
        jto.expandRow(4);
        jto.expandRow(5);
        jto.selectRow(8);
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUp"), 1).push();  // NOI18N
        jto.expandRow(11);
        jto.selectRow(14);
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonDown")).push();  // NOI18N

        // Test edit dialog and cancel button
        jto.expandRow(0);
        jto.selectRow(0);
        textField = new JTextFieldOperator(editFrame, 0);
        textField.clickMouse();
        textField.setText("XYZ");
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonCancel")).push();  // NOI18N

        jto.collapseRow(0);
        jto.selectRow(0);

        // Test adding a layout and its components
        new JButtonOperator(editFrame, Bundle.getMessage("AddLayoutButtonText")).push();  // NOI18N
        textField = new JTextFieldOperator(editFrame, 0);
        textField.clickMouse();
        textField.setText("Test Layout");  // NOI18N
        textField = new JTextFieldOperator(editFrame, 1);
        textField.clickMouse();
        textField.setText("6");
        textField = new JTextFieldOperator(editFrame, 2);
        textField.clickMouse();
        textField.setText("5");
        new JCheckBoxOperator(editFrame, 0).doClick();
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        jto.expandRow(1);
        jto.selectRow(2);

        new JButtonOperator(editFrame, Bundle.getMessage("AddTrainTypeButtonText")).push();  // NOI18N
        textField = new JTextFieldOperator(editFrame, 0);
        textField.clickMouse();
        textField.setText("New Train Type");  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        jto.selectRow(4);

        new JButtonOperator(editFrame, Bundle.getMessage("AddSegmentButtonText")).push();  // NOI18N
        textField = new JTextFieldOperator(editFrame, 0);
        textField.clickMouse();
        textField.setText("Mainline");  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        new JButtonOperator(editFrame, Bundle.getMessage("AddStationButtonText")).push();  // NOI18N
        textField = new JTextFieldOperator(editFrame, 0);
        textField.clickMouse();
        textField.setText("Station 1");  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        jto.selectRow(5);

        new JButtonOperator(editFrame, Bundle.getMessage("AddStationButtonText")).push();  // NOI18N
        textField = new JTextFieldOperator(editFrame, 0);
        textField.clickMouse();
        textField.setText("Station 2");  // NOI18N
        textField = new JTextFieldOperator(editFrame, 1);
        textField.clickMouse();
        textField.setText("50");
        new JCheckBoxOperator(editFrame, 0).doClick();
        new JSpinnerOperator(editFrame, 0).setValue(1);
        new JSpinnerOperator(editFrame, 1).setValue(3);
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        jto.selectRow(8);

        new JButtonOperator(editFrame, Bundle.getMessage("AddScheduleButtonText")).push();  // NOI18N
        textField = new JTextFieldOperator(editFrame, 0);
        textField.clickMouse();
        textField.setText("Test Schedule");  // NOI18N
        textField = new JTextFieldOperator(editFrame, 1);
        textField.clickMouse();
        textField.setText("Today");  // NOI18N
        new JSpinnerOperator(editFrame, 0).setValue(1);
        new JSpinnerOperator(editFrame, 1).setValue(23);
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        new JButtonOperator(editFrame, Bundle.getMessage("AddTrainButtonText")).push();  // NOI18N
        textField = new JTextFieldOperator(editFrame, 0);
        textField.clickMouse();
        textField.setText("TRN");  // NOI18N
        textField = new JTextFieldOperator(editFrame, 1);
        textField.clickMouse();
        textField.setText("Test Train");  // NOI18N
        textField = new JTextFieldOperator(editFrame, 2);
        textField.clickMouse();
        textField.setText("10");
        textField = new JTextFieldOperator(editFrame, 3);
        textField.clickMouse();
        textField.setText("12:00");
        new JComboBoxOperator(editFrame, 0).selectItem("New Train Type");  // NOI18N
        new JSpinnerOperator(editFrame, 0).setValue(1);
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        new JButtonOperator(editFrame, Bundle.getMessage("AddStopButtonText")).push();  // NOI18N
        textField = new JTextFieldOperator(editFrame, 0);
        textField.clickMouse();
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        jto.selectRow(10);

        new JButtonOperator(editFrame, Bundle.getMessage("AddStopButtonText")).push();  // NOI18N
        new JComboBoxOperator(editFrame, 0).selectItem("Station 2");  // NOI18N
        textField = new JTextFieldOperator(editFrame, 1);
        textField.clickMouse();
        textField.setText("20");
        new JSpinnerOperator(editFrame, 0).setValue(1);
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        // Delete the layout in reverse order
        jto.selectRow(12);
        new JButtonOperator(editFrame, Bundle.getMessage("DeleteStopButtonText")).push();  // NOI18N
        jto.selectRow(11);
        new JButtonOperator(editFrame, Bundle.getMessage("DeleteStopButtonText")).push();  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("DeleteTrainButtonText")).push();  // NOI18N

        jto.selectRow(9);
        new JButtonOperator(editFrame, Bundle.getMessage("DeleteScheduleButtonText")).push();  // NOI18N

        jto.selectRow(7);
        new JButtonOperator(editFrame, Bundle.getMessage("DeleteStationButtonText")).push();  // NOI18N
        jto.selectRow(6);
        new JButtonOperator(editFrame, Bundle.getMessage("DeleteStationButtonText")).push();  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("DeleteSegmentButtonText")).push();  // NOI18N

        jto.selectRow(3);
        new JButtonOperator(editFrame, Bundle.getMessage("DeleteTrainTypeButtonText")).push();  // NOI18N

        jto.selectRow(1);
        Thread t1 = createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonYes"));  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("DeleteLayoutButtonText")).push();  // NOI18N
        t1.join();

        // Misc tests
        ttf.makeDetailGrid("XYZ");  // NOI18N
        jmri.util.JUnitAppender.assertWarnMessage("Invalid grid type: 'XYZ'");  // NOI18N

        // Other buttons
        jto.clickOnPath(jto.findPath(new String[]{"Sample", "Segments", "Mainline"}));
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonGraph")).push();  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonSave")).push();  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonDone")).push();  // NOI18N
    }

    Thread createModalDialogOperatorThread(String dialogTitle, String buttonText) {
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(dialogTitle);
            JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
            jbo.pushNoBlock();
        });
        t.setName(dialogTitle + " Close Dialog Thread");  // NOI18N
        t.start();
        return t;
    }

    @BeforeClass
    public static void setUp() {
        jmri.util.JUnitUtil.setUp();

        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();

        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ttf = new TimeTableFrame("");
        ttf.setVisible(true);
    }

    @AfterClass
    public static void tearDown() {
        ttf = null;
        jmri.util.JUnitUtil.tearDown();
    }
}