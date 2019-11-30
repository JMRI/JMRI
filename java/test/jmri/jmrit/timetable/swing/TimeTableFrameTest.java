package jmri.jmrit.timetable.swing;

import java.awt.GraphicsEnvironment;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import jmri.jmrit.timetable.*;
import jmri.util.JUnitUtil;
import org.junit.*;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.*;

/**
 * Tests for the TimeTableFrame Class
 * @author Dave Sand Copyright (C) 2018
 */
public class TimeTableFrameTest {

    @Rule
    public org.junit.rules.TemporaryFolder folder = new org.junit.rules.TemporaryFolder();

    TimeTableFrame _ttf = null;
    JFrameOperator _jfo = null;
    JTreeOperator _jto = null;
    JTextFieldOperator _jtxt = null;
    JButtonOperator _jbtn = null;

    @Test
    public void testCreatEmpty() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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
        deleteTests();
        addTests();
        deleteLayout();
        addTests();
        deleteSections();
        editTests();
        timeRangeTests();
        deleteDialogTests();
        buttonTests();
    }

// new EventTool().waitNoEvent(10000);

    void menuTests() {
        JMenuBarOperator jmbo = new JMenuBarOperator(_jfo); // there's only one menubar
        JMenuOperator jmo = new JMenuOperator(jmbo, Bundle.getMessage("MenuTimetable"));  // NOI18N
        JPopupMenu jpm = jmo.getPopupMenu();

        JMenuItem timeMenuItem = (JMenuItem)jpm.getComponent(0);
        Assert.assertTrue(timeMenuItem.getText().equals(Bundle.getMessage("MenuTrainTimes")));  // NOI18N
        new JMenuItemOperator(timeMenuItem).doClick();

        JMenuItem twoPageMenuItem = (JMenuItem)jpm.getComponent(2);
        Assert.assertTrue(twoPageMenuItem.getText().equals(Bundle.getMessage("MenuTwoPage")));  // NOI18N
        new JMenuItemOperator(twoPageMenuItem).doClick();

        Thread openDialog = createModalDialogOperatorThread("Open", Bundle.getMessage("ButtonCancel"), "openDialog");  // NOI18N
        JMenuItem importMenuItem = (JMenuItem)jpm.getComponent(4);
        Assert.assertTrue(importMenuItem.getText().equals(Bundle.getMessage("MenuImportSgn")));  // NOI18N
        new JMenuItemOperator(importMenuItem).doClick();
        JUnitUtil.waitFor(()->{return !(openDialog.isAlive());}, "open dialog finished");
    }

    void addTests() {
        // Add a new layout
        _jto.clickOnPath(_jto.findPath(new String[]{"Sample"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("AddLayoutButtonText")).doClick();  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();
        _jtxt.setText("Time Table Frame Test Layout");  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 1);
        _jtxt.clickMouse();
        _jtxt.setText("6");
        _jtxt = new JTextFieldOperator(_jfo, 2);
        _jtxt.clickMouse();
        _jtxt.setText("5");
        new JCheckBoxOperator(_jfo, 0).doClick();
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).doClick();  // NOI18N

        // Add a train type
        _jto.clickOnPath(_jto.findPath(new String[]{"Time Table Frame Test Layout", "Train Types"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("AddTrainTypeButtonText")).doClick();  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();
        _jtxt.setText("New Train Type");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).doClick();  // NOI18N

        // Add a segment
        _jto.clickOnPath(_jto.findPath(new String[]{"Time Table Frame Test Layout", "Segments"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("AddSegmentButtonText")).doClick();  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();
        _jtxt.setText("Mainline");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).doClick();  // NOI18N

        // Add a station 1
        _jto.clickOnPath(_jto.findPath(new String[]{"Time Table Frame Test Layout", "Segments", "Mainline"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("AddStationButtonText")).doClick();  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();
        _jtxt.setText("Station 1");  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 1);
        _jtxt.clickMouse();
        _jtxt.setText("0");
        new JCheckBoxOperator(_jfo, 0).doClick();
        new JSpinnerOperator(_jfo, 0).setValue(1);
        new JSpinnerOperator(_jfo, 1).setValue(1);
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).doClick();  // NOI18N

        // Add a station 2
        _jto.clickOnPath(_jto.findPath(new String[]{"Time Table Frame Test Layout", "Segments", "Mainline"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("AddStationButtonText")).doClick();  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();
        _jtxt.setText("Station 2");  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 1);
        _jtxt.clickMouse();
        _jtxt.setText("50");
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).doClick();  // NOI18N

        // Add a schedule
        _jto.clickOnPath(_jto.findPath(new String[]{"Time Table Frame Test Layout", "Schedules"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("AddScheduleButtonText")).doClick();  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();
        _jtxt.setText("Test Schedule");  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 1);
        _jtxt.clickMouse();
        _jtxt.setText("Today");
        new JSpinnerOperator(_jfo, 0).setValue(1);
        new JSpinnerOperator(_jfo, 1).setValue(22);
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).doClick();  // NOI18N

        // Add a train
        _jto.clickOnPath(_jto.findPath(new String[]{"Time Table Frame Test Layout", "Schedules",  // NOI18N
                "Test Schedule   Effective Date: Today"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("AddTrainButtonText")).doClick();  // NOI18N
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
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).doClick();  // NOI18N

        // Add stop 1
        new JButtonOperator(_jfo, Bundle.getMessage("AddStopButtonText")).doClick();  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).doClick();  // NOI18N

        // Add stop 2
        _jto.clickOnPath(_jto.findPath(new String[]{"Time Table Frame Test Layout", "Schedules",  // NOI18N
                "Test Schedule   Effective Date: Today", "TRN -- Test Train"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("AddStopButtonText")).doClick();  // NOI18N
        new JComboBoxOperator(_jfo, 0).selectItem("Station 2");  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 1);
        _jtxt.clickMouse();
        _jtxt.setText("20");
//         new JSpinnerOperator(_jfo, 0).setValue(1);
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).doClick();  // NOI18N

        JLabelOperator jlo = new JLabelOperator(_jfo, 7);
        Assert.assertEquals("14:37", jlo.getText());
    }

    void editTests() {
        // Layout: Bad fastclock value, good value to force recalc, throttle too low.
        _jto.clickOnPath(_jto.findPath(new String[]{"Sample"}));  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 1);
        _jtxt.clickMouse();
        _jtxt.setText("bad fast clock");  // NOI18N
        Thread edit1 = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"), "edit1");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).doClick();  // NOI18N
        JUnitUtil.waitFor(()->{return !(edit1.isAlive());}, "edit1 finished");  // NOI18N
        jmri.util.JUnitAppender.assertWarnMessage("'bad fast clock' is not a valid number for fast clock");

        _jtxt = new JTextFieldOperator(_jfo, 1);
        _jtxt.clickMouse();
        _jtxt.setText("5");  // NOI18N

        _jtxt = new JTextFieldOperator(_jfo, 2);
        _jtxt.clickMouse();
        _jtxt.setText("1");  // NOI18N
        Thread edit2 = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"), "edit2");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).doClick();  // NOI18N
        JUnitUtil.waitFor(()->{return !(edit2.isAlive());}, "edit2 finished");  // NOI18N

        // Change scale
        // NOTE:  This section may cause issues in the future.  The JComboBoxOperator
        // appears to select entry 0 before selecting the specified entry.
        // Since CUSTOM is entry zero, the custom listener is triggered which puts
        // up dialog box.  By setting the combo box before making edit mode
        // active (text field click), the dialog box goes away and the actual
        // selected scale entry is shown.
        new JComboBoxOperator(_jfo, 0).selectItem("N (160.0)");  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).doClick();  // NOI18N
        Assert.assertEquals(new JLabelOperator(_jfo, 6).getText(), "6.60 feet");

        // Station:  Distance and staging track.
        _jto.clickOnPath(_jto.findPath(new String[]{"Sample", "Segments", "Mainline", "Alpha"}));  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 1);
        _jtxt.clickMouse();
        _jtxt.setText("-5.0");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).doClick();  // NOI18N

        _jtxt.clickMouse();
        _jtxt.setText("bad distance");  // NOI18N
        Thread edit3 = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"), "edit3");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).doClick();  // NOI18N
        JUnitUtil.waitFor(()->{return !(edit3.isAlive());}, "edit3 finished");  // NOI18N

        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();  // Activate Update button
        new JSpinnerOperator(_jfo, 1).setValue(3);
        Thread edit4 = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"), "edit4");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).doClick();  // NOI18N
        JUnitUtil.waitFor(()->{return !(edit4.isAlive());}, "edit4 finished");  // NOI18N

        // Schedule:  Start hour and duration.
        _jto.clickOnPath(_jto.findPath(new String[]{"Sample", "Schedule", "114"}));  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();  // Activate Update button
        new JSpinnerOperator(_jfo, 0).setValue(30);
        new JSpinnerOperator(_jfo, 1).setValue(30);
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).doClick();  // NOI18N

        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();  // Activate Update button
        new JSpinnerOperator(_jfo, 0).setValue(1);
        new JSpinnerOperator(_jfo, 1).setValue(22);
        Thread edit5 = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"), "edit5");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).doClick();  // NOI18N
        JUnitUtil.waitFor(()->{return !(edit5.isAlive());}, "edit5 finished");

        // Train:  Speed, Start time, Notes
        _jto.clickOnPath(_jto.findPath(new String[]{"Sample", "Schedule", "114", "AMX"}));  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 2);
        _jtxt.clickMouse();
        _jtxt.setText("-5");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).doClick();  // NOI18N

        _jtxt = new JTextFieldOperator(_jfo, 3);
        _jtxt.clickMouse();
        _jtxt.setText("1234");  // NOI18N
        Thread edit6 = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"), "edit6");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).doClick();  // NOI18N
        JUnitUtil.waitFor(()->{return !(edit6.isAlive());}, "edit6 finished");

        JTextAreaOperator textArea = new JTextAreaOperator(_jfo, 0);
        textArea.clickMouse();
        textArea.setText(java.util.UUID.randomUUID().toString());  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).doClick();  // NOI18N

        // Stop:  Duration, next speed, notes
        _jto.clickOnPath(_jto.findPath(new String[]{"Sample", "Schedule", "114", "AMX", "1"}));  // NOI18N

        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();
        _jtxt.setText("-5");  // NOI18N

        _jtxt = new JTextFieldOperator(_jfo, 1);
        _jtxt.clickMouse();
        _jtxt.setText("-5");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).doClick();  // NOI18N

        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();
        _jtxt.setText("5");  // NOI18N

        textArea = new JTextAreaOperator(_jfo, 0);
        textArea.clickMouse();
        textArea.setText(java.util.UUID.randomUUID().toString());  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).doClick();  // NOI18N

        // Indirect layout listener veto tests
        try {
            jmri.ScaleManager.getScale("N").setScaleRatio(500.0);
        } catch (java.beans.PropertyVetoException ex) {
        }

        try {
            jmri.ScaleManager.getScale("UK-N").setScaleRatio(150.0);
        } catch (Exception ex) {
        }
    }

    void timeRangeTests() {
        // Change schedule duration
        _jto.clickOnPath(_jto.findPath(new String[]{"Sample", "Schedule", "123"}));  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();  // Activate Update button
        new JSpinnerOperator(_jfo, 1).setValue(6);
        Thread time1 = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"), "@@ time1");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).doClick();  // NOI18N
        JUnitUtil.waitFor(()->{return !(time1.isAlive());}, "time1 finished");

        // Change train start time
        _jto.clickOnPath(_jto.findPath(new String[]{"Sample", "Schedule", "123", "EXP"}));  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 3);
        _jtxt.clickMouse();
        _jtxt.setText("9:00");  // NOI18N
        Thread time2 = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"), "time2");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).doClick();  // NOI18N
        JUnitUtil.waitFor(()->{return !(time2.isAlive());}, "time2 finished");

        // Change train start time to move stop times outside of schedule
        _jto.clickOnPath(_jto.findPath(new String[]{"Sample", "Schedule", "123", "EXP"}));  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 3);
        _jtxt.clickMouse();
        _jtxt.setText("14:00");  // NOI18N
        Thread time3 = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"), "time3");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).doClick();  // NOI18N
        JUnitUtil.waitFor(()->{return !(time3.isAlive());}, "time3 finished");

        // Change stop duration to move stop times outside of schedule
        _jto.clickOnPath(_jto.findPath(new String[]{"Sample", "Schedule", "123", "EXP", "5"}));  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();
        _jtxt.setText("240");  // NOI18N
        Thread time4 = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"), "time4");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUpdate")).doClick();  // NOI18N
        JUnitUtil.waitFor(()->{return !(time4.isAlive());}, "time4 finished");
    }

    void deleteTests() {
        // Delete the test layout created by the add process
        _jto.clickOnPath(_jto.findPath(new String[]{"Test", "Schedules", "Test", "TRN", "2"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteStopButtonText")).doClick();  // NOI18N
        _jto.clickOnPath(_jto.findPath(new String[]{"Test", "Schedules", "Test", "TRN", "1"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteStopButtonText")).doClick();  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteTrainButtonText")).doClick();  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteScheduleButtonText")).doClick();  // NOI18N
        _jto.clickOnPath(_jto.findPath(new String[]{"Test", "Segments", "Mainline", "Station 2"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteStationButtonText")).doClick();  // NOI18N
        _jto.clickOnPath(_jto.findPath(new String[]{"Test", "Segments", "Mainline", "Station 1"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteStationButtonText")).doClick();  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteSegmentButtonText")).doClick();  // NOI18N
        _jto.clickOnPath(_jto.findPath(new String[]{"Test", "Train Types", "New"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteTrainTypeButtonText")).doClick();  // NOI18N
        _jto.clickOnPath(_jto.findPath(new String[]{"Test"}));  // NOI18N

        Thread finalDelete = createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonYes"), "finalDelete");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteLayoutButtonText")).doClick();  // NOI18N
        JUnitUtil.waitFor(()->{return !(finalDelete.isAlive());}, "finalDelete finished");
    }

    void deleteDialogTests() {
        _jto.clickOnPath(_jto.findPath(new String[]{"Sample"}));  // NOI18N
        Thread delLayout = createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonNo"), "delLayout");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteLayoutButtonText")).doClick();  // NOI18N
        JUnitUtil.waitFor(()->{return !(delLayout.isAlive());}, "delLayout finished");

        _jto.clickOnPath(_jto.findPath(new String[]{"Sample", "Train Types", "Yard"}));  // NOI18N
        Thread delTrainType = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"), "delTrainType");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteTrainTypeButtonText")).doClick();  // NOI18N
        JUnitUtil.waitFor(()->{return !(delTrainType.isAlive());}, "delTrainType finished");  // NOI18N

        _jto.clickOnPath(_jto.findPath(new String[]{"Sample", "Segments", "Mainline"}));  // NOI18N
        Thread delSegment = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"), "delSegment");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteSegmentButtonText")).doClick();  // NOI18N
        JUnitUtil.waitFor(()->{return !(delSegment.isAlive());}, "delSegment finished");  // NOI18N

        _jto.clickOnPath(_jto.findPath(new String[]{"Sample", "Segments", "Mainline", "Alpha"}));  // NOI18N
        Thread delStation = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"), "delStation");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteStationButtonText")).doClick();  // NOI18N
        JUnitUtil.waitFor(()->{return !(delStation.isAlive());}, "delStation finished");  // NOI18N

        _jto.clickOnPath(_jto.findPath(new String[]{"Sample", "Schedules", "114   Effective Date: 11/4/93"}));  // NOI18N
        Thread delSchedule = createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonNo"), "delSchedule");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteScheduleButtonText")).doClick();  // NOI18N
        JUnitUtil.waitFor(()->{return !(delSchedule.isAlive());}, "delSchedule finished");  // NOI18N

        _jto.clickOnPath(_jto.findPath(new String[]{"Sample", "Schedules", "114   Effective Date: 11/4/93", "AMX -- Morning Express"}));  // NOI18N
        Thread delTrain = createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonNo"), "delTrain");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteTrainButtonText")).doClick();  // NOI18N
        JUnitUtil.waitFor(()->{return !(delTrain.isAlive());}, "delTrain finished");  // NOI18N
    }

    void deleteLayout() {
        _jto.clickOnPath(_jto.findPath(new String[]{"Test"}));  // NOI18N
        Thread layoutDelete = createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonYes"), "layoutDelete");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteLayoutButtonText")).doClick();  // NOI18N
        JUnitUtil.waitFor(()->{return !(layoutDelete.isAlive());}, "layoutDelete finished");
    }

    void deleteSections() {
        // Train and stops
        _jto.clickOnPath(_jto.findPath(new String[]{"Test", "Schedules", "Test", "TRN"}));  // NOI18N
        Thread section1 = createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonYes"), "section1");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteTrainButtonText")).doClick();  // NOI18N
        JUnitUtil.waitFor(()->{return !(section1.isAlive());}, "section1 finished");

        // Add a train back to force dialog for schedule delete
        _jto.clickOnPath(_jto.findPath(new String[]{"Test", "Schedules", "Test"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("AddTrainButtonText")).doClick();  // NOI18N

        // Delete schedule and train
        _jto.clickOnPath(_jto.findPath(new String[]{"Test", "Schedules", "Test"}));  // NOI18N
        Thread section2= createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonYes"), "section2");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteScheduleButtonText")).doClick();  // NOI18N
        JUnitUtil.waitFor(()->{return !(section2.isAlive());}, "section2 finished");

        // Delete segment and stations
        _jto.clickOnPath(_jto.findPath(new String[]{"Test", "Segments", "Mainline"}));  // NOI18N
        Thread section3 = createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonYes"), "section3");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteSegmentButtonText")).doClick();  // NOI18N
        JUnitUtil.waitFor(()->{return !(section3.isAlive());}, "section3 finished");

        // Delete layout
        _jto.clickOnPath(_jto.findPath(new String[]{"Test"}));  // NOI18N
        Thread section4 = createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonYes"), "section4");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("DeleteLayoutButtonText")).doClick();  // NOI18N
        JUnitUtil.waitFor(()->{return !(section4.isAlive());}, "section4 finished");
    }

    void buttonTests() {
        // Test move buttons
        _jto.clickOnPath(_jto.findPath(new String[]{"Sample", "Schedule", "114", "AMX", "3"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonUp"), 1).doClick();  // NOI18N
        _jto.clickOnPath(_jto.findPath(new String[]{"Sample", "Schedule", "114", "AO", "3"}));  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonDown")).doClick();  // NOI18N

        // Test edit dialog and cancel button
        _jto.clickOnPath(_jto.findPath(new String[]{"Sample"}));  // NOI18N
        _jtxt = new JTextFieldOperator(_jfo, 0);
        _jtxt.clickMouse();
        _jtxt.setText("XYZ");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonCancel")).doClick();  // NOI18N

        // Misc tests
        _ttf.makeDetailGrid("XYZ");  // NOI18N
        jmri.util.JUnitAppender.assertWarnMessage("Invalid grid type: 'XYZ'");  // NOI18N
        _ttf.showNodeEditMessage();  // NOI18N

        _jto.clickOnPath(_jto.findPath(new String[]{"Sample", "Segments", "Mainline"}));  // NOI18N
        Thread misc1 = createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonOK"), "misc1");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonDisplay")).doClick();  // NOI18N
        JUnitUtil.waitFor(()->{return !(misc1.isAlive());}, "misc1 finished");

        // Other buttons
        Thread misc2 = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonNo"), "misc2");  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonDone")).doClick();  // NOI18N
        JUnitUtil.waitFor(()->{return !(misc2.isAlive());}, "misc2 finished");
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonSave")).doClick();  // NOI18N
        new JButtonOperator(_jfo, Bundle.getMessage("ButtonDone")).doClick();  // NOI18N
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

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

        JUnitUtil.resetInstanceManager();
        try {
            JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder.newFolder(jmri.profile.Profile.PROFILE)));
        } catch(java.io.IOException ioe){
          Assert.fail("failed to setup profile for test");
        }
    }

    @After
    public  void tearDown() {
       // use reflection to reset the static file location.
       try {
            Class<?> c = jmri.jmrit.timetable.configurexml.TimeTableXml.TimeTableXmlFile.class;
            java.lang.reflect.Field f = c.getDeclaredField("fileLocation");
            f.setAccessible(true);
            f.set(new String(), null);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException x) {
            Assert.fail("Failed to reset TimeTableXml static fileLocation " + x);
        }
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimeTableFrameTest.class);
}
