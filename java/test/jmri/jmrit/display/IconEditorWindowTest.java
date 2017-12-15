package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
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
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.rules.Timeout;
import org.junit.runner.Description;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Swing tests for the SensorIcon
 *
 * @author	Bob Jacobsen Copyright 2009, 2010
 */
public class IconEditorWindowTest {

    @Rule
    public TestRule watcher = new TestWatcher() {
        @Override
        protected void starting(Description description) {
            if (Boolean.valueOf(System.getenv("TRAVIS_PULL_REQUEST"))) {
                // use System.out.println instead of logging to avoid using
                // warning or error while still providing this output on PRs
                // in Travis CI (and blocking elsewhere)
                System.out.println("Starting test: " + description.getMethodName());
            }
        }
    };

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10); // 10 second timeout for methods in this test class.

    Editor _editor = null;
    JComponent _panel;

    @Test
    public void testSensorEditor() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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

        jmri.util.ThreadingUtil.runOnGUI(() -> {
            icon.setLocation(x, y);
            _panel.repaint();
        });

        Assert.assertEquals("initial state", Sensor.UNKNOWN, sensor.getState());

        JFrameOperator iefo = new JFrameOperator(iconEditorFrame);
        JComponentOperator jfo = new JComponentOperator(_panel);
        int xloc = icon.getLocation().x + icon.getSize().width / 2;
        int yloc = icon.getLocation().y + icon.getSize().height / 2;
        jfo.clickMouse(xloc, yloc, 1);

        // this will wait for WAITFOR_MAX_DELAY (15 seconds) max
        // checking the condition every WAITFOR_DELAY_STEP (5 mSecs)
        // if it's still false after max wait it throws an assert.
        JUnitUtil.waitFor(() -> {
            return sensor.getState() == Sensor.INACTIVE;
        }, "state after one click");

        // Click icon change state to inactive
        jfo.clickMouse(xloc, yloc, 1);
        JUnitUtil.waitFor(() -> {
            return sensor.getState() == Sensor.ACTIVE;
        }, "state after two clicks");

        iefo.requestClose();
    }

    @Test
    public void testRightTOEditor() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't Assume in TestCase
        }
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

        jmri.util.ThreadingUtil.runOnGUI(() -> {
            icon.setLocation(x, y);
            _panel.repaint();
        });

        Assert.assertEquals("initial state", Sensor.UNKNOWN, turnout.getState());
        JFrameOperator iefo = new JFrameOperator(iconEditorFrame);
        JComponentOperator jfo = new JComponentOperator(_panel);
        int xloc = icon.getLocation().x + icon.getSize().width / 2;
        int yloc = icon.getLocation().y + icon.getSize().height / 2;
        jfo.clickMouse(xloc, yloc, 1);

        JUnitUtil.waitFor(() -> {
            return turnout.getState() == Turnout.CLOSED;
        }, "state after one click");

        // Click icon change state to inactive
        jfo.clickMouse(xloc, yloc, 1);
        JUnitUtil.waitFor(() -> {
            return turnout.getState() == Turnout.THROWN;
        }, "state after two clicks");

        iefo.requestClose();
    }

    @Test
    public void testLeftTOEditor() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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

        jmri.util.ThreadingUtil.runOnGUI(() -> {
            icon.setLocation(x, y);
            _panel.repaint();
        });

        java.awt.Point location = new java.awt.Point(x + icon.getSize().width / 2,
                y + icon.getSize().height / 2);

        Assert.assertEquals("initial state", Sensor.UNKNOWN, turnout.getState());

        JFrameOperator iefo = new JFrameOperator(iconEditorFrame);
        JComponentOperator jfo = new JComponentOperator(_panel);
        int xloc = icon.getLocation().x + icon.getSize().width / 2;
        int yloc = icon.getLocation().y + icon.getSize().height / 2;
        jfo.clickMouse(xloc, yloc, 1);

        JUnitUtil.waitFor(() -> {
            return turnout.getState() == Turnout.CLOSED;
        }, "state after one click");

        // Click icon change state to inactive
        jfo.clickMouse(xloc, yloc, 1);
        JUnitUtil.waitFor(() -> {
            return turnout.getState() == Turnout.THROWN;
        }, "state after two clicks");

        iefo.requestClose();
    }

    @Test
    public void testLightEditor() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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

        JFrameOperator iefo = new JFrameOperator(iconEditorFrame);
        JComponentOperator jfo = new JComponentOperator(_panel);
        int xloc = icon.getLocation().x + icon.getSize().width / 2;
        int yloc = icon.getLocation().y + icon.getSize().height / 2;
        jfo.clickMouse(xloc, yloc, 1);

        JUnitUtil.waitFor(() -> {
            return light.getState() == Light.ON;
        }, "state after one click");

        // Click icon change state to inactive
        jfo.clickMouse(xloc, yloc, 1);
        JUnitUtil.waitFor(() -> {
            return light.getState() == Light.OFF;
        }, "state after two clicks");

        iefo.requestClose();
    }

    @Test
    public void testSignalHeadEditor() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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

        JFrameOperator iefo = new JFrameOperator(iconEditorFrame);
        JComponentOperator jfo = new JComponentOperator(_panel);
        int xloc = icon.getLocation().x + icon.getSize().width / 2;
        int yloc = icon.getLocation().y + icon.getSize().height / 2;
        jfo.clickMouse(xloc, yloc, 1);

        for (int i = 1; i < states.length; i++) {
            //Assert.assertEquals("state after " + i + " click", states[i], signalHead.getState());
            final int state = states[i];
            // this will wait for WAITFOR_MAX_DELAY (15 seconds) max
            // checking the condition every WAITFOR_DELAY_STEP (5 mSecs)
            // if it's still false after max wait it throws an assert.
            JUnitUtil.waitFor(() -> {
                return signalHead.getState() == state;
            }, "state after " + i + " click(s)");

            jfo.clickMouse(xloc, yloc, 1);
        }

        iefo.requestClose();
    }

    @Test
    public void testMemoryEditor() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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

        JFrameOperator iefo = new JFrameOperator(iconEditorFrame);
        JComponentOperator jfo = new JComponentOperator(_panel);
        int xloc = memIcon.getLocation().x + memIcon.getSize().width / 2;
        int yloc = memIcon.getLocation().y + memIcon.getSize().height / 2;
        jfo.clickMouse(xloc, yloc, 1);

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

        xloc = memIcon.getLocation().x + memSpinIcon.getSize().width / 2;
        yloc = memIcon.getLocation().y + memSpinIcon.getSize().height / 2;
        jfo.clickMouse(xloc, yloc, 1);

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

        xloc = memIcon.getLocation().x + memInputIcon.getSize().width / 2;
        yloc = memIcon.getLocation().y + memInputIcon.getSize().height / 2;
        jfo.clickMouse(xloc, yloc, 1);

        iefo.requestClose();
    }

    @Test
    public void testReporterEditor() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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

        JFrameOperator iefo = new JFrameOperator(iconEditorFrame);
        JComponentOperator jfo = new JComponentOperator(_panel);
        int xloc = icon.getLocation().x + icon.getSize().width / 2;
        int yloc = icon.getLocation().y + icon.getSize().height / 2;
        jfo.clickMouse(xloc, yloc, 1);

        iefo.requestClose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initShutDownManager();

        if (!GraphicsEnvironment.isHeadless()) {
            _editor = new PanelEditor("IconEditorTestPanel");
            Assert.assertNotNull(JFrameOperator.waitJFrame("IconEditorTestPanel", true, true));
            _panel = _editor.getTargetPanel();
            Assert.assertNotNull(_panel);
        }
    }

    @After
    public void tearDown() throws Exception {

        // Delete the editor by calling dispose(true) defined in PanelEditor
        // directly instead of closing the window through a WindowClosing()
        // event - this is the method called to delete a panel if a user
        // selects that in the Hide/Delete dialog triggered by WindowClosing().
        if (_editor != null) {
            _editor.dispose(true);
        }

        JUnitUtil.resetWindows(false, false); // don't log existing windows here, should just be from this class
        JUnitUtil.tearDown();
    }
}
