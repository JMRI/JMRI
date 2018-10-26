package jmri.jmrit.timetable.swing;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.timetable.*;
import jmri.util.JUnitUtil;
import org.junit.*;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.*;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;

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
        Assert.assertNotNull(jto);
        JTextFieldOperator textField;

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
        JUnitUtil.waitFor(()->{return !(t1.isAlive());}, "t1 finished");

        // Misc tests
        ttf.makeDetailGrid("XYZ");  // NOI18N
        jmri.util.JUnitAppender.assertWarnMessage("Invalid grid type: 'XYZ'");  // NOI18N

        jto.clickOnPath(jto.findPath(new String[]{"Sample", "Segments", "Mainline"}));  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonGraph")).push();  // NOI18N

        // Menu tests
        JMenuBarOperator jmbo = new JMenuBarOperator(editFrame); // there's only one menubar
        JMenuOperator jmo = new JMenuOperator(jmbo, Bundle.getMessage("MenuTimetable"));  // NOI18N
        JPopupMenu jpm = jmo.getPopupMenu();

        JMenuItem timeMenuItem = (JMenuItem)jpm.getComponent(0);
        Assert.assertTrue(timeMenuItem.getText().equals(Bundle.getMessage("MenuTrainTimes")));  // NOI18N
        new JMenuItemOperator(timeMenuItem).doClick();

        Thread fileOpen = createModalDialogOperatorThread("Open", Bundle.getMessage("ButtonCancel"));  // NOI18N
        JMenuItem importMenuItem = (JMenuItem)jpm.getComponent(2);
        Assert.assertTrue(importMenuItem.getText().equals(Bundle.getMessage("MenuImport")));  // NOI18N
        new JMenuItemOperator(importMenuItem).doClick();
        JUnitUtil.waitFor(()->{return !(fileOpen.isAlive());}, "open dialog finished");

        // Test edits
        // Layout: Bad fastclock value, throttle too low.
        jto.clickOnPath(jto.findPath(new String[]{"Sample"}));  // NOI18N
        textField = new JTextFieldOperator(editFrame, 1);
        textField.clickMouse();
        textField.setText("bad fast clock");  // NOI18N
        Thread edit1 = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"));  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N
        JUnitUtil.waitFor(()->{return !(edit1.isAlive());}, "edit1 finished");

        textField = new JTextFieldOperator(editFrame, 2);
        textField.clickMouse();
        textField.setText("1");  // NOI18N
        Thread edit2 = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"));  // NOI18N
        log.warn("find and push the update button");
        JButtonOperator bEdit2 = new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate"));  // NOI18N
        bEdit2.push();
        JUnitUtil.waitFor(()->{return !(edit2.isAlive());}, "edit2 finished");

        // Station:  Distance and staging track.
        jto.clickOnPath(jto.findPath(new String[]{"Sample", "Segments", "Mainline", "Alpha"}));  // NOI18N
        textField = new JTextFieldOperator(editFrame, 1);
        textField.clickMouse();
        textField.setText("-5.0");  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        textField.clickMouse();
        textField.setText("bad distance");  // NOI18N
        Thread edit3 = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"));  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N
        JUnitUtil.waitFor(()->{return !(edit3.isAlive());}, "edit3 finished");

        textField = new JTextFieldOperator(editFrame, 0);
        textField.clickMouse();  // Activate Update button
        new JSpinnerOperator(editFrame, 1).setValue(0);
        Thread edit4 = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"));  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N
        JUnitUtil.waitFor(()->{return !(edit4.isAlive());}, "edit4 finished");

        // Schedule:  Start hour and duration.
        jto.clickOnPath(jto.findPath(new String[]{"Sample", "Schedule", "114   Effective Date: 11/4/93"}));  // NOI18N
        log.warn("sched = {}", new JTextFieldOperator(editFrame, 0).getText());
        textField = new JTextFieldOperator(editFrame, 0);
        textField.clickMouse();  // Activate Update button
        new JSpinnerOperator(editFrame, 0).setValue(30);
        new JSpinnerOperator(editFrame, 1).setValue(30);
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        textField = new JTextFieldOperator(editFrame, 0);
        textField.clickMouse();  // Activate Update button
        new JSpinnerOperator(editFrame, 0).setValue(1);
        new JSpinnerOperator(editFrame, 1).setValue(22);
        Thread edit5 = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"));  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N
        JUnitUtil.waitFor(()->{return !(edit5.isAlive());}, "edit5 finished");

        // Train:  Speed, Start time, Notes
        jto.clickOnPath(jto.findPath(new String[]{"Sample", "Schedule", "114   Effective Date: 11/4/93", "AMX -- Morning Express"}));  // NOI18N
        log.warn("train = {}", new JTextFieldOperator(editFrame, 0).getText());
        textField = new JTextFieldOperator(editFrame, 2);
        textField.clickMouse();
        textField.setText("-5");  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        textField = new JTextFieldOperator(editFrame, 3);
        textField.clickMouse();
        textField.setText("1234");  // NOI18N
        Thread edit6 = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"));  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N
        JUnitUtil.waitFor(()->{return !(edit6.isAlive());}, "edit6 finished");

// --------------  23-20    start time range error

        JTextAreaOperator textArea = new JTextAreaOperator(editFrame, 0);
        textArea.clickMouse();
        textArea.setText("A train note");  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        // Stop:  Duration, next speed, notes
        jto.clickOnPath(jto.findPath(new String[]{"Sample", "Schedule", "114   Effective Date: 11/4/93", "AMX -- Morning Express", "1 -- Omega"}));  // NOI18N
        log.warn("stop = {}", new JTextFieldOperator(editFrame, 0).getText());

        textField = new JTextFieldOperator(editFrame, 0);
        textField.clickMouse();
        textField.setText("-5");  // NOI18N

        textField = new JTextFieldOperator(editFrame, 1);
        textField.clickMouse();
        textField.setText("-5");  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        textField = new JTextFieldOperator(editFrame, 0);
        textField.clickMouse();
        textField.setText("5");  // NOI18N

        textArea = new JTextAreaOperator(editFrame, 0);
        textArea.clickMouse();
        textArea.setText("A stop note");  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

// javax.swing.JFrame delayFrame = JFrameOperator.waitJFrame("Delay Title", true, true);



        // Other buttons
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
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimeTableFrameTest.class);
}