package jmri.jmrit.display;

import javax.swing.JComponent;
import jmri.InstanceManager;
import jmri.Light;
import jmri.Memory;
import jmri.Reporter;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.jmrit.display.panelEditor.PanelEditor;
import jmri.util.JUnitUtil;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.eventdata.EventDataConstants;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Swing jfcUnit tests for the SensorIcon
 *
 * @author	Bob Jacobsen Copyright 2009, 2010
 */
public class IconEditorWindowTest extends jmri.util.SwingTestCase {

    Editor _editor;
    JComponent _panel;

    public void testSensorEditor() throws Exception {

        _editor.addSensorEditor();

        Editor.JFrameItem iconEditorFrame = _editor.getIconFrame("Sensor");
        IconAdder iconEditor = iconEditorFrame.getEditor();
        Assert.assertNotNull(iconEditor);

        iconEditor._sysNametext.setText("IS0");
        iconEditor.addToTable();

        SensorIcon icon = _editor.putSensor();
        Assert.assertNotNull(icon);
        Sensor sensor = icon.getSensor();
        Assert.assertNotNull(sensor);

        int x = 50;
        int y = 20;

        jmri.util.ThreadingUtil.runOnGUI(()->{
            icon.setLocation(x, y);
            _panel.repaint();
        });

        java.awt.Point location = new java.awt.Point(x + icon.getSize().width / 2,
                y + icon.getSize().height / 2);

        Assert.assertEquals("initial state", Sensor.UNKNOWN, sensor.getState());

        getHelper().enterClickAndLeave(
                new MouseEventData(this,
                        _panel, // component
                        1, // number clicks
                        EventDataConstants.DEFAULT_MOUSE_MODIFIERS, // modifiers
                        false, // isPopUpTrigger
                        10, // sleeptime
                        EventDataConstants.CUSTOM, // position
                        location
                ));
        Assert.assertEquals("state after one click", Sensor.INACTIVE, sensor.getState());

        // Click icon change state to inactive
        getHelper().enterClickAndLeave(new MouseEventData(this, icon));
        Assert.assertEquals("state after two clicks", Sensor.ACTIVE, sensor.getState());

        TestHelper.disposeWindow(iconEditorFrame, this);
    }

    public void testRightTOEditor() throws Exception {

        Editor.JFrameItem iconEditorFrame = _editor.getIconFrame("RightTurnout");
        IconAdder iconEditor = iconEditorFrame.getEditor();
        Assert.assertNotNull(iconEditor);

        iconEditor._sysNametext.setText("IT0");
        iconEditor.addToTable();

        TurnoutIcon icon = _editor.addTurnout(iconEditor);
        Assert.assertNotNull(icon);
        Turnout turnout = icon.getTurnout();
        Assert.assertNotNull(turnout);

        int x = 30;
        int y = 10;

        jmri.util.ThreadingUtil.runOnGUI(()->{
            icon.setLocation(x, y);
            _panel.repaint();
        });

        java.awt.Point location = new java.awt.Point(x + icon.getSize().width / 2,
                y + icon.getSize().height / 2);

        Assert.assertEquals("initial state", Sensor.UNKNOWN, turnout.getState());

        getHelper().enterClickAndLeave(
                new MouseEventData(this,
                        _panel, // component
                        1, // number clicks
                        EventDataConstants.DEFAULT_MOUSE_MODIFIERS, // modifiers
                        false, // isPopUpTrigger
                        10, // sleeptime
                        EventDataConstants.CUSTOM, // position
                        location
                ));
        Assert.assertEquals("state after one click", Turnout.CLOSED, turnout.getState());

        // Click icon change state to inactive
        getHelper().enterClickAndLeave(new MouseEventData(this, icon));
        Assert.assertEquals("state after two clicks", Turnout.THROWN, turnout.getState());

        TestHelper.disposeWindow(iconEditorFrame, this);
    }

    public void testLeftTOEditor() throws Exception {

        Editor.JFrameItem iconEditorFrame = _editor.getIconFrame("LeftTurnout");
        IconAdder iconEditor = iconEditorFrame.getEditor();
        Assert.assertNotNull(iconEditor);

        iconEditor._sysNametext.setText("IT1");
        iconEditor.addToTable();

        TurnoutIcon icon = _editor.addTurnout(iconEditor);
        Assert.assertNotNull(icon);
        Turnout turnout = icon.getTurnout();
        Assert.assertNotNull(turnout);

        int x = 30;
        int y = 10;

        jmri.util.ThreadingUtil.runOnGUI(()->{
            icon.setLocation(x, y);
            _panel.repaint();
        });

        java.awt.Point location = new java.awt.Point(x + icon.getSize().width / 2,
                y + icon.getSize().height / 2);

        Assert.assertEquals("initial state", Sensor.UNKNOWN, turnout.getState());

        getHelper().enterClickAndLeave(
                new MouseEventData(this,
                        _panel, // component
                        1, // number clicks
                        EventDataConstants.DEFAULT_MOUSE_MODIFIERS, // modifiers
                        false, // isPopUpTrigger
                        10, // sleeptime
                        EventDataConstants.CUSTOM, // position
                        location
                ));
        Assert.assertEquals("state after one click", Turnout.CLOSED, turnout.getState());

        // Click icon change state to inactive
        getHelper().enterClickAndLeave(new MouseEventData(this, icon));
        Assert.assertEquals("state after two clicks", Turnout.THROWN, turnout.getState());

        TestHelper.disposeWindow(iconEditorFrame, this);
    }

    public void testLightEditor() throws Exception {

        Editor.JFrameItem iconEditorFrame = _editor.getIconFrame("Light");
        IconAdder iconEditor = iconEditorFrame.getEditor();
        Assert.assertNotNull(iconEditor);

        iconEditor._sysNametext.setText("IL0");
        iconEditor.addToTable();

        LightIcon icon = _editor.addLight();
        Assert.assertNotNull(icon);
        Light light = icon.getLight();
        Assert.assertNotNull(light);

        int x = 30;
        int y = 10;
        icon.setLocation(x, y);
        _panel.repaint();

        java.awt.Point location = new java.awt.Point(x + icon.getSize().width / 2,
                y + icon.getSize().height / 2);

        Assert.assertEquals("initial state", Light.OFF, light.getState());

        getHelper().enterClickAndLeave(
                new MouseEventData(this,
                        _panel, // component
                        1, // number clicks
                        EventDataConstants.DEFAULT_MOUSE_MODIFIERS, // modifiers
                        false, // isPopUpTrigger
                        10, // sleeptime
                        EventDataConstants.CUSTOM, // position
                        location
                ));
        Assert.assertEquals("state after one click", Light.ON, light.getState());

        // Click icon change state to inactive
        getHelper().enterClickAndLeave(new MouseEventData(this, icon));
        Assert.assertEquals("state after two clicks", Light.OFF, light.getState());

        TestHelper.disposeWindow(iconEditorFrame, this);
    }

    public void testSignalHeadEditor() throws Exception {

        Editor.JFrameItem iconEditorFrame = _editor.getIconFrame("SignalHead");
        IconAdder iconEditor = iconEditorFrame.getEditor();
        Assert.assertNotNull(iconEditor);

        SignalHead signalHead = new jmri.implementation.VirtualSignalHead("IH0");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(signalHead);

        iconEditor.setSelection(signalHead);

        SignalHeadIcon icon = _editor.putSignalHead();
        Assert.assertNotNull(icon);
        SignalHead sh = icon.getSignalHead();
        Assert.assertEquals("SignalHead==sh", signalHead, sh);

        int x = 30;
        int y = 10;
        icon.setLocation(x, y);
        _panel.repaint();

        java.awt.Point location = new java.awt.Point(x + icon.getSize().width / 2,
                y + icon.getSize().height / 2);

        int[] states = signalHead.getValidStates();
        Assert.assertEquals("initial state", states[0], signalHead.getState());

        getHelper().enterClickAndLeave(
                new MouseEventData(this,
                        _panel, // component
                        1, // number clicks
                        EventDataConstants.DEFAULT_MOUSE_MODIFIERS, // modifiers
                        false, // isPopUpTrigger
                        10, // sleeptime
                        EventDataConstants.CUSTOM, // position
                        location
                ));

        for (int i = 1; i < states.length; i++) {
            Assert.assertEquals("state after " + i + " click", states[i], signalHead.getState());
            getHelper().enterClickAndLeave(new MouseEventData(this, icon));
        }

        TestHelper.disposeWindow(iconEditorFrame, this);
    }

    public void testMemoryEditor() throws Exception {

        Editor.JFrameItem iconEditorFrame = _editor.getIconFrame("Memory");
        IconAdder iconEditor = iconEditorFrame.getEditor();
        Assert.assertNotNull(iconEditor);

        iconEditor._sysNametext.setText("IM0");
        iconEditor.addToTable();

        MemoryIcon memIcon = _editor.putMemory();
        Assert.assertNotNull(memIcon);
        Memory memory = memIcon.getMemory();
        Assert.assertNotNull(memory);

        int x = 20;
        int y = 10;
        memIcon.setLocation(x, y);
        _panel.repaint();

        java.awt.Point location = new java.awt.Point(x + memIcon.getSize().width / 2,
                y + memIcon.getSize().height / 2);
        getHelper().enterClickAndLeave(
                new MouseEventData(this,
                        _panel, // component
                        1, // number clicks
                        EventDataConstants.DEFAULT_MOUSE_MODIFIERS, // modifiers
                        false, // isPopUpTrigger
                        10, // sleeptime
                        EventDataConstants.CUSTOM, // position
                        location
                ));

        iconEditor._sysNametext.setText("IM1");
        iconEditor.addToTable();

        MemorySpinnerIcon memSpinIcon = _editor.addMemorySpinner();
        Assert.assertNotNull(memSpinIcon);
        memory = memSpinIcon.getMemory();
        Assert.assertNotNull(memory);

        x = 70;
        y = 10;
        memSpinIcon.setLocation(x, y);
        _panel.repaint();

        location = new java.awt.Point(x + memSpinIcon.getSize().width / 2,
                y + memSpinIcon.getSize().height / 2);
        getHelper().enterClickAndLeave(
                new MouseEventData(this,
                        _panel, // component
                        1, // number clicks
                        EventDataConstants.DEFAULT_MOUSE_MODIFIERS, // modifiers
                        false, // isPopUpTrigger
                        10, // sleeptime
                        EventDataConstants.CUSTOM, // position
                        location
                ));

        iconEditor._sysNametext.setText("IM2");
        iconEditor.addToTable();

        MemoryInputIcon memInputIcon = _editor.addMemoryInputBox();
        Assert.assertNotNull(memInputIcon);
        memory = memInputIcon.getMemory();
        Assert.assertNotNull(memory);

        x = 150;
        y = 10;
        memInputIcon.setLocation(x, y);
        _panel.repaint();

        location = new java.awt.Point(x + memInputIcon.getSize().width / 2,
                y + memInputIcon.getSize().height / 2);
        getHelper().enterClickAndLeave(
                new MouseEventData(this,
                        _panel, // component
                        1, // number clicks
                        EventDataConstants.DEFAULT_MOUSE_MODIFIERS, // modifiers
                        false, // isPopUpTrigger
                        10, // sleeptime
                        EventDataConstants.CUSTOM, // position
                        location
                ));

        TestHelper.disposeWindow(iconEditorFrame, this);
    }

    public void testReporterEditor() throws Exception {

        Editor.JFrameItem iconEditorFrame = _editor.getIconFrame("Reporter");
        IconAdder iconEditor = iconEditorFrame.getEditor();
        Assert.assertNotNull(iconEditor);

        iconEditor._sysNametext.setText("IR0");
        iconEditor.addToTable();

        ReporterIcon icon = _editor.addReporter();
        Assert.assertNotNull(icon);
        Reporter reporter = icon.getReporter();
        Assert.assertNotNull(reporter);

        int x = 30;
        int y = 10;
        icon.setLocation(x, y);
        _panel.repaint();

        java.awt.Point location = new java.awt.Point(x + icon.getSize().width / 2, y + icon.getSize().height / 2);

        getHelper().enterClickAndLeave(
                new MouseEventData(this,
                        _panel, // component
                        1, // number clicks
                        EventDataConstants.DEFAULT_MOUSE_MODIFIERS, // modifiers
                        false, // isPopUpTrigger
                        10, // sleeptime
                        EventDataConstants.CUSTOM, // position
                        location
                ));

        TestHelper.disposeWindow(iconEditorFrame, this);
    }

    /**
     * ***********************************************************************************
     */
    // from here down is testing infrastructure
    public IconEditorWindowTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", IconEditorWindowTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(IconEditorWindowTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initShutDownManager();

        jmri.util.ThreadingUtil.runOnGUI(()->{
            _editor = new PanelEditor("IconEditorTestPanel");
            Assert.assertNotNull(_editor);
            _panel = _editor.getTargetPanel();
            Assert.assertNotNull(_panel);
        });
    }

    @Override
    protected void tearDown() throws Exception {

        // Delete the editor by calling dispose(true) defined in PanelEditor
        // directly instead of closing the window through a WindowClosing()
        // event - this is the method called to delete a panel if a user
        // selects that in the Hide/Delete dialog triggered by WindowClosing().
        _editor.dispose(true);

        JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }
}
