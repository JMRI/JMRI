package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;

import javax.swing.*;

import jmri.jmrit.display.EditorFrameOperator;
import jmri.jmrit.display.layoutEditor.*;
import jmri.util.*;
import jmri.util.swing.JemmyUtil;

import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.*;

/**
 * Test simple functioning of LayoutTurntableEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutTurntableEditorTest extends LayoutTrackEditorTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        new LayoutTurntableEditor(null);
    }


     @Test
    public void testEditTurntableDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        createTurnouts();

        LayoutTurntableEditor editor = new LayoutTurntableEditor(layoutEditor);

        // Edit the layoutTurntable
        editor.editLayoutTrack(layoutTurntableView);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTurntable"));

        // Set good radius
        JLabelOperator jLabelOperator = new JLabelOperator(
                jFrameOperator, Bundle.getMessage("TurntableRadius"));
        JTextFieldOperator jtxt = new JTextFieldOperator(
                (JTextField) jLabelOperator.getLabelFor());
        jtxt.setText("30");

        // Add 5 rays
        JButtonOperator addRayTrackJButtonOperator = new JButtonOperator(
                jFrameOperator, Bundle.getMessage("AddRayTrack"));
        jLabelOperator = new JLabelOperator(
                jFrameOperator, Bundle.getMessage("RayAngle"));
        jtxt = new JTextFieldOperator(
                (JTextField) jLabelOperator.getLabelFor());

        addRayTrackJButtonOperator.doClick();
        jtxt.setText("90");
        addRayTrackJButtonOperator.doClick();
        jtxt.setText("180");
        addRayTrackJButtonOperator.doClick();
        jtxt.setText("270");
        addRayTrackJButtonOperator.doClick();
        jtxt.setText("315");
        addRayTrackJButtonOperator.doClick();

        // Delete the 5th ray
        Thread deleteRay = JemmyUtil.createModalDialogOperatorThread(
                "Warning", "Yes");  // NOI18N
        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDelete"), 4).doClick();
        JUnitUtil.waitFor(() -> {
            return !(deleteRay.isAlive());
        }, "deleteRay finished");

        // Enable DCC control
        new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("TurntableDCCControlled")).doClick();

        // Change the first ray to 30 degrees
        JLabelOperator rayAngleLabelOperator = new JLabelOperator(
                jFrameOperator, Bundle.getMessage("MakeLabel", Bundle.getMessage("RayAngle")), 0);
        jtxt = new JTextFieldOperator((JTextField) rayAngleLabelOperator.getLabelFor());
        jtxt.setText("30");

        // Set ray turnouts
        //TODO: fix hardcoded index
        JComboBoxOperator turnout_cbo = new JComboBoxOperator(jFrameOperator, 1);
        JComboBoxOperator state_cbo = new JComboBoxOperator(jFrameOperator, 2);

        turnout_cbo.selectItem(1); //TODO: fix hardcoded index
        state_cbo.selectItem(0); //TODO: fix hardcoded index

        turnout_cbo = new JComboBoxOperator(jFrameOperator, 3);
        state_cbo = new JComboBoxOperator(jFrameOperator, 4);
        turnout_cbo.selectItem(1); //TODO: fix hardcoded index
        state_cbo.selectItem(1); //TODO: fix hardcoded index

        turnout_cbo = new JComboBoxOperator(jFrameOperator, 5);
        state_cbo = new JComboBoxOperator(jFrameOperator, 6);
        turnout_cbo.selectItem(2); //TODO: fix hardcoded index
        state_cbo.selectItem(0); //TODO: fix hardcoded index

        turnout_cbo = new JComboBoxOperator(jFrameOperator, 7);
        state_cbo = new JComboBoxOperator(jFrameOperator, 8);
        turnout_cbo.selectItem(2); //TODO: fix hardcoded index
        state_cbo.selectItem(1); //TODO: fix hardcoded index

        // Add a valid ray and then change the angle to an invalid value
        jtxt = new JTextFieldOperator(jFrameOperator, 3);
        jtxt.clickMouse();
        jtxt.setText("qqq");


        // Move focus
        Thread badRayAngleModalDialogOperatorThread = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        jtxt = new JTextFieldOperator(jFrameOperator, 4); // elsewhere to fire error
        jtxt.clickMouse();
        JUnitUtil.waitFor(() -> {
            return !(badRayAngleModalDialogOperatorThread.isAlive());
        }, "badRayAngle finished");

        // Put a good value back in
        jtxt = new JTextFieldOperator(jFrameOperator, 3);
        jtxt.clickMouse();
        jtxt.setText("0");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed

        //jmri.util.JUnitAppender.assertErrorMessage("provideLayoutBlock: no name given and not assigning auto block names");
    }

    @Test
    public void testEditTurntableCancel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        LayoutTurntableEditor editor = new LayoutTurntableEditor(layoutEditor);

        // Edit the Turntable
        editor.editLayoutTrack(layoutTurntableView);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTurntable"));

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel")).doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }

    @Test
    public void testEditTurntableClose() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        LayoutTurntableEditor editor = new LayoutTurntableEditor(layoutEditor);

        // Edit the Turntable
        editor.editLayoutTrack(layoutTurntableView);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTurntable"));

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed

    }

    @Test
    public void testEditTurntableErrors() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        LayoutTurntableEditor editor = new LayoutTurntableEditor(layoutEditor);

        // Edit the layoutTurntable
        editor.editLayoutTrack(layoutTurntableView);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTurntable"));

        // Ray angle
        JLabelOperator jLabelOperator = new JLabelOperator(
                jFrameOperator, Bundle.getMessage("RayAngle"));
        JTextFieldOperator jtxt = new JTextFieldOperator(
                (JTextField) jLabelOperator.getLabelFor());
        jtxt.setText("xyz");

        Thread badAngle = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  // NOI18N

        JButtonOperator addRayTrackJButtonOperator = new JButtonOperator(
                jFrameOperator, Bundle.getMessage("AddRayTrack"));
        addRayTrackJButtonOperator.doClick();

        JUnitUtil.waitFor(() -> {
            return !(badAngle.isAlive());
        }, "badAngle finished");

        // Set radius
        jLabelOperator = new JLabelOperator(
                jFrameOperator, Bundle.getMessage("TurntableRadius"));
        jtxt = new JTextFieldOperator(
                (JTextField) jLabelOperator.getLabelFor());
        jtxt.setText("abc");

        Thread badRadius = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  // NOI18N

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();

        JUnitUtil.waitFor(() -> {
            return !(badRadius.isAlive());
        }, "badRadius finished");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel")).doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed

    }

    private LayoutEditor layoutEditor = null;
    private LayoutTurntableView layoutTurntableView = null;
    private LayoutTurntable layoutTurntable = null;

    @BeforeEach
    public void setUp() {
        super.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initLayoutBlockManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        if (!GraphicsEnvironment.isHeadless()) {

            layoutEditor = new LayoutEditor();
            layoutEditor.setVisible(true);

            Point2D point = new Point2D.Double(150.0, 100.0);
            Point2D delta = new Point2D.Double(50.0, 10.0);

            // Turntable
            point = MathUtil.add(point, delta);
            layoutTurntable = new LayoutTurntable("Turntable", layoutEditor);
            layoutTurntableView = new LayoutTurntableView(layoutTurntable, point, layoutEditor);
            layoutEditor.addLayoutTrack(layoutTurntable, layoutTurntableView);
        }
    }

    @AfterEach
    public void tearDown() {
        if (layoutTurntable != null) {
            layoutTurntable.remove();
        }

        if (layoutEditor != null) {
            EditorFrameOperator efo = new EditorFrameOperator(layoutEditor);
            efo.closeFrameWithConfirmations();
        }

        layoutTurntable = null;
        layoutEditor = null;

        JUnitUtil.resetWindows(false, false);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        super.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTurntableEditorTest.class);
}
