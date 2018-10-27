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
    TimeTableFrame _ttf = null;
    JFrameOperator _jfo = null;
    JTreeOperator _jto = null;
    JTextFieldOperator _jtxt = null;
    JButtonOperator _jbtn = null;

    @Test
    public void testCreatEmtpy() {
        TimeTableFrame f = new TimeTableFrame();
        Assert.assertNotNull(f);
    }

    @Test
    public void testDriver() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        _ttf = new TimeTableFrame("");
        Assert.assertNotNull(_ttf);
        _ttf.setVisible(true);
        _jfo = new JFrameOperator(Bundle.getMessage("TitleTimeTable"));  // NOI18N
        Assert.assertNotNull(_jfo);
        _jto = new JTreeOperator(_jfo);
        Assert.assertNotNull(_jto);

        menuTests();
        addTests();
        editTests();
        deleteTests();
        deleteDialogTests();
        buttonTests();
// new EventTool().waitNoEvent(10000);
    }  // TODO

    void menuTests() {
        log.warn("-- menu tests");
        JMenuBarOperator jmbo = new JMenuBarOperator(_jfo); // there's only one menubar
        JMenuOperator jmo = new JMenuOperator(jmbo, Bundle.getMessage("MenuTimetable"));  // NOI18N
        JPopupMenu jpm = jmo.getPopupMenu();

        JMenuItem timeMenuItem = (JMenuItem)jpm.getComponent(0);
        Assert.assertTrue(timeMenuItem.getText().equals(Bundle.getMessage("MenuTrainTimes")));  // NOI18N
        new JMenuItemOperator(timeMenuItem).doClick();

        Thread openDialog = createModalDialogOperatorThread("Open", Bundle.getMessage("ButtonCancel"));  // NOI18N
        JMenuItem importMenuItem = (JMenuItem)jpm.getComponent(2);
        Assert.assertTrue(importMenuItem.getText().equals(Bundle.getMessage("MenuImport")));  // NOI18N
        new JMenuItemOperator(importMenuItem).doClick();
        JUnitUtil.waitFor(()->{return !(openDialog.isAlive());}, "open dialog finished");
    }

    void addTests() {
        log.warn("-- add tests");

        // Add a new layout
        _jto.clickOnPath(_jto.findPath(new String[]{"Sample"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("AddLayoutButtonText")).push();  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();
        _jtxt.setText("Test Layout");  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 1);
        _jtxt.clickMouse();
        _jtxt.setText("6");
        _jtxt = new JTextFieldOperator(_jfo, 2);
        _jtxt.clickMouse();
        _jtxt.setText("5");
        new JCheckBoxOperator(_jfo, 0).doClick();
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        // Add a train type
        _jto.clickOnPath(_jto.findPath(new String[]{"Test Layout", "Train Types"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("AddTrainTypeButtonText")).push();  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();
        _jtxt.setText("New Train Type");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        // Add a segment
        _jto.clickOnPath(_jto.findPath(new String[]{"Test Layout", "Segments"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("AddSegmentButtonText")).push();  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();
        _jtxt.setText("Mainline");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        // Add a station 1
        _jto.clickOnPath(_jto.findPath(new String[]{"Test Layout", "Segments", "Mainline"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("AddStationButtonText")).push();  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();
        _jtxt.setText("Station 1");  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 1);
        _jtxt.clickMouse();
        _jtxt.setText("0");
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        // Add a station 2
        _jto.clickOnPath(_jto.findPath(new String[]{"Test Layout", "Segments", "Mainline"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("AddStationButtonText")).push();  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();
        _jtxt.setText("Station 2");  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 1);
        _jtxt.clickMouse();
        _jtxt.setText("50");
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        // Add a schedule
        _jto.clickOnPath(_jto.findPath(new String[]{"Test Layout", "Schedules"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("AddScheduleButtonText")).push();  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();
        _jtxt.setText("Test Schedule");  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 1);
        _jtxt.clickMouse();
        _jtxt.setText("Today");
        new JSpinnerOperator(_jfo, 0).setValue(1);
        new JSpinnerOperator(_jfo, 1).setValue(22);
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        // Add a train
        _jto.clickOnPath(_jto.findPath(new String[]{"Test Layout", "Schedules",  // NOI18N
                "Test Schedule   Effective Date: Today"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("AddTrainButtonText")).push();  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();
        _jtxt.setText("TRN");  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 1);
        _jtxt.clickMouse();
        _jtxt.setText("Test Train");  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 2);
        _jtxt.clickMouse();
        _jtxt.setText("10");
        _jtxt = new JTextFieldOperator(_jfo, 3);
        _jtxt.clickMouse();
        _jtxt.setText("12:00");
        new JComboBoxOperator(_jfo, 0).selectItem("New Train Type");  // NOI18N
        new JSpinnerOperator(_jfo, 0).setValue(1);
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        // Add stop 1
        new JButtonOperator(_jfo, Bundle.getMessage("AddStopButtonText")).push();  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        // Add stop 2
        _jto.clickOnPath(_jto.findPath(new String[]{"Test Layout", "Schedules",  // NOI18N
                "Test Schedule   Effective Date: Today", "TRN -- Test Train"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("AddStopButtonText")).push();  // NOI18N
        new JComboBoxOperator(_jfo, 0).selectItem("Station 2");  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 1);
        _jtxt.clickMouse();
        _jtxt.setText("20");
        new JSpinnerOperator(_jfo, 0).setValue(1);
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        JLabelOperator jlo = new JLabelOperator(_jfo, 7);
        Assert.assertEquals("14:37", jlo.getText());
    }

    void editTests() {
        log.warn("-- edit tests");
        // Layout: Bad fastclock value, good value to force recalc, throttle too low.
        _jto.clickOnPath(_jto.findPath(new String[]{"Sample"}));  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 1);
        _jtxt.clickMouse();
        _jtxt.setText("bad fast clock");  // NOI18N
        Thread edit1 = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N
        JUnitUtil.waitFor(()->{return !(edit1.isAlive());}, "edit1 finished");  // NOI18N

        _jtxt = new JTextFieldOperator(_jfo, 1);
        _jtxt.clickMouse();
        _jtxt.setText("5");  // NOI18N

        _jtxt = new JTextFieldOperator(_jfo, 2);
        _jtxt.clickMouse();
        _jtxt.setText("1");  // NOI18N
        Thread edit2 = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N
        JUnitUtil.waitFor(()->{return !(edit2.isAlive());}, "edit2 finished");  // NOI18N

        // Station:  Distance and staging track.
        _jto.clickOnPath(_jto.findPath(new String[]{"Sample", "Segments", "Mainline", "Alpha"}));  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 1);
        _jtxt.clickMouse();
        _jtxt.setText("-5.0");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        _jtxt.clickMouse();
        _jtxt.setText("bad distance");  // NOI18N
        Thread edit3 = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N
        JUnitUtil.waitFor(()->{return !(edit3.isAlive());}, "edit3 finished");  // NOI18N

        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();  // Activate Update button
        new JSpinnerOperator(_jfo, 1).setValue(0);
        Thread edit4 = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N
        JUnitUtil.waitFor(()->{return !(edit4.isAlive());}, "edit4 finished");  // NOI18N

        // Schedule:  Start hour and duration.
        _jto.clickOnPath(_jto.findPath(new String[]{"Sample", "Schedule", "114"}));  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();  // Activate Update button
        new JSpinnerOperator(_jfo, 0).setValue(30);
        new JSpinnerOperator(_jfo, 1).setValue(30);
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();  // Activate Update button
        new JSpinnerOperator(_jfo, 0).setValue(1);
        new JSpinnerOperator(_jfo, 1).setValue(22);
        Thread edit5 = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N
        JUnitUtil.waitFor(()->{return !(edit5.isAlive());}, "edit5 finished");

        // Train:  Speed, Start time, Notes
        _jto.clickOnPath(_jto.findPath(new String[]{"Sample", "Schedule", "114", "AMX"}));  // NOI18N
        log.warn("train = {}", new JTextFieldOperator(_jfo, 0).getText());
        _jtxt = new JTextFieldOperator(_jfo, 2);
        _jtxt.clickMouse();
        _jtxt.setText("-5");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        _jtxt = new JTextFieldOperator(_jfo, 3);
        _jtxt.clickMouse();
        _jtxt.setText("1234");  // NOI18N
        Thread edit6 = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N
        JUnitUtil.waitFor(()->{return !(edit6.isAlive());}, "edit6 finished");

// // --------------  23-20    start time range error

        JTextAreaOperator textArea = new JTextAreaOperator(_jfo, 0);
        textArea.clickMouse();
        textArea.setText("A train note");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        // Stop:  Duration, next speed, notes
        _jto.clickOnPath(_jto.findPath(new String[]{"Sample", "Schedule", "114", "AMX", "1"}));  // NOI18N
        log.warn("stop = {}", new JTextFieldOperator(_jfo, 0).getText());

        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();
        _jtxt.setText("-5");  // NOI18N

        _jtxt = new JTextFieldOperator(_jfo, 1);
        _jtxt.clickMouse();
        _jtxt.setText("-5");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();
        _jtxt.setText("5");  // NOI18N

        textArea = new JTextAreaOperator(_jfo, 0);
        textArea.clickMouse();
        textArea.setText("A stop note");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N
    } // TODO

    void deleteTests() {
        log.warn("-- delete tests");
        // Delete the test layout created by the add process
        _jto.clickOnPath(_jto.findPath(new String[]{"Test", "Schedules", "Test", "TRN", "2"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteStopButtonText")).push();  // NOI18N
        _jto.clickOnPath(_jto.findPath(new String[]{"Test", "Schedules", "Test", "TRN", "1"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteStopButtonText")).push();  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteTrainButtonText")).push();  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteScheduleButtonText")).push();  // NOI18N
        _jto.clickOnPath(_jto.findPath(new String[]{"Test", "Segments", "Mainline", "Station 2"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteStationButtonText")).push();  // NOI18N
        _jto.clickOnPath(_jto.findPath(new String[]{"Test", "Segments", "Mainline", "Station 1"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteStationButtonText")).push();  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteSegmentButtonText")).push();  // NOI18N
        _jto.clickOnPath(_jto.findPath(new String[]{"Test", "Train Types", "New"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteTrainTypeButtonText")).push();  // NOI18N
        _jto.clickOnPath(_jto.findPath(new String[]{"Test"}));  // NOI18N

        Thread finalDelete = createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonYes"));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteLayoutButtonText")).push();  // NOI18N
        JUnitUtil.waitFor(()->{return !(finalDelete.isAlive());}, "finalDelete finished");
    }

    void deleteDialogTests() {
        log.warn("-- delete tests, reply no/ok to dialog");
        _jto.clickOnPath(_jto.findPath(new String[]{"Sample"}));  // NOI18N
        Thread delLayout = createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonNo"));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteLayoutButtonText")).push();  // NOI18N
        JUnitUtil.waitFor(()->{return !(delLayout.isAlive());}, "delLayout finished");

        _jto.clickOnPath(_jto.findPath(new String[]{"Sample", "Train Types", "Yard"}));  // NOI18N
        Thread delTrainType = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteTrainTypeButtonText")).push();  // NOI18N
        JUnitUtil.waitFor(()->{return !(delTrainType.isAlive());}, "delTrainType finished");  // NOI18N

        _jto.clickOnPath(_jto.findPath(new String[]{"Sample", "Segments", "Mainline"}));  // NOI18N
        Thread delSegment = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteSegmentButtonText")).push();  // NOI18N
        JUnitUtil.waitFor(()->{return !(delSegment.isAlive());}, "delSegment finished");  // NOI18N

        _jto.clickOnPath(_jto.findPath(new String[]{"Sample", "Segments", "Mainline", "Alpha"}));  // NOI18N
        Thread delStation = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteStationButtonText")).push();  // NOI18N
        JUnitUtil.waitFor(()->{return !(delStation.isAlive());}, "delStation finished");  // NOI18N

        _jto.clickOnPath(_jto.findPath(new String[]{"Sample", "Schedules", "114   Effective Date: 11/4/93"}));  // NOI18N
        Thread delSchedule = createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonNo"));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteScheduleButtonText")).push();  // NOI18N
        JUnitUtil.waitFor(()->{return !(delSchedule.isAlive());}, "delSchedule finished");  // NOI18N

        _jto.clickOnPath(_jto.findPath(new String[]{"Sample", "Schedules", "114   Effective Date: 11/4/93", "AMX -- Morning Express"}));  // NOI18N
        Thread delTrain = createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonNo"));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteTrainButtonText")).push();  // NOI18N
        JUnitUtil.waitFor(()->{return !(delTrain.isAlive());}, "delTrain finished");  // NOI18N
    }

    void buttonTests() {
        log.warn("-- button tests");
        // Test move buttons
        _jto.clickOnPath(_jto.findPath(new String[]{"Sample", "Schedule", "114", "AMX", "3"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUp"), 1).push();  // NOI18N
        _jto.clickOnPath(_jto.findPath(new String[]{"Sample", "Schedule", "114", "AO", "3"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonDown")).push();  // NOI18N

        // Test edit dialog and cancel button
        _jto.clickOnPath(_jto.findPath(new String[]{"Sample"}));  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();
        _jtxt.setText("XYZ");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonCancel")).push();  // NOI18N

        // Misc tests
        _ttf.makeDetailGrid("XYZ");  // NOI18N
        jmri.util.JUnitAppender.assertWarnMessage("Invalid grid type: 'XYZ'");  // NOI18N

        _jto.clickOnPath(_jto.findPath(new String[]{"Sample", "Segments", "Mainline"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonGraph")).push();  // NOI18N

        // Other buttons
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonSave")).push();  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonDone")).push();  // NOI18N
    }

    Thread createModalDialogOperatorThread(String dialogTitle, String buttonText) {
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(dialogTitle);
            log.warn("jdo = {}", jdo);
            JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
            jbo.pushNoBlock();
        });
        t.setName(dialogTitle + " Close Dialog Thread");  // NOI18N
        t.start();
        return t;
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
    }

    @After
    public  void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimeTableFrameTest.class);
}