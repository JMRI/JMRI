package jmri.jmrit.display.layoutEditor.layoutEditorTools;

import static java.lang.Thread.sleep;

import java.awt.GraphicsEnvironment;
import java.util.logging.Level;
import java.util.logging.Logger;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutEditorFindItems;
import jmri.jmrit.display.layoutEditor.PositionablePoint;
import jmri.util.JUnitUtil;
import jmri.util.MathUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test simple functioning of layoutEditorToolsFrame
 *
 * @author	George Warner Copyright (C) 2017
 */
public class LayoutEditorToolsFrameTest {

    private static LayoutEditor layoutEditor = null;

    @BeforeClass
    public static void setupClass() {
        if (!GraphicsEnvironment.isHeadless()) {
            try {
                jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
                };
                Assert.assertNotNull("ConfigXmlManager exists", cm);

                // load and display sample file
                java.io.File leFile = new java.io.File("java/test/jmri/jmrit/display/layoutEditor/valid/SimpleLayoutEditorTest.xml");
                cm.load(leFile);
                sleep(100); // time for internal listeners to calm down

                // Find new window by name (should be more distinctive, comes from sample file)
                layoutEditor = (LayoutEditor) jmri.util.JmriJFrame.getFrame("My Layout");

                // make it editable
                layoutEditor.setAllEditable(true);
                Assert.assertTrue("isEditable after setAllEditable(true)", layoutEditor.isEditable());
            } catch (Exception ex) {
                Logger.getLogger(LayoutEditorToolsFrameTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @AfterClass
    public static void teardownClass() {
        //TODO: REMOVE for production
        //System.out.println("LayoutEditorToolsFrameTest.teardownClass();");
        if (layoutEditor != null) {
            layoutEditor.dispose();
            layoutEditor = null;
        }
    }

//    @Test
//    public void testSetSignalsAtBoundary() {
//        LayoutEditorToolsSetSignalsAtBoundaryFrameOperator op = new LayoutEditorToolsSetSignalsAtBoundaryFrameOperator();
//        Assert.assertNotNull("Operator exists", op);
//        op.setJComboBoxText("A", "B");
//    }

    @Test
    public void testPositionablePointPopup() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertNotNull("LayoutEditor exists", layoutEditor);

        LayoutEditorFindItems leFinder = layoutEditor.getFinder();
        Assert.assertNotNull("LayoutEditorFindItems exists", leFinder);

        // find End Bumper
        PositionablePoint ppEB1 = leFinder.findPositionablePointByName("EB1");
        Assert.assertNotNull("End Bumper EB1 exists", ppEB1);
        java.awt.Point location = MathUtil.PointForPoint2D(MathUtil.center(ppEB1.getBounds()));

        //redTarget.mouse().click(1, redTarget.wrap().getClickPoint(), Mouse.MouseButtons.BUTTON1, Keyboard.KeyboardModifiers.SHIFT_DOWN_MASK);
        //redTarget.mouse().click(1, redTarget.wrap().getClickPoint(), MouseEvent.BUTTON1, ActionEvent.SHIFT_MASK);
        //checkMouseEvent(RED, MouseEvent.MOUSE_CLICKED, MouseButton.PRIMARY, 50, 50, 1);
        //
        //TODO: REMOVE for production;
        // (only here so developer can see what's happening)
        try {
            sleep(3000);
        } catch (InterruptedException ex) {
            Logger.getLogger(LayoutEditorToolsFrameTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void testSetInputs() {
        // this test only checks to see that we don't throw an exception when
        // setting the input values.
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
//        layoutEditorToolsFrame frame = new layoutEditorToolsFrame();
//        frame.setInputs("IS1", "IS2", "IS3", "5280", "5280");
    }

    @Test
    public void testVerifyInputsValid() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
//        layoutEditorToolsFrame frame = new layoutEditorToolsFrame();
//        // set the input values
//        frame.setInputs("IS1", "IS2", "IS3", "5280", "5280");
//        // then use reflection to call verifyInputs
//        java.lang.reflect.Method verifyInputsValidMethod = null;
//        try {
//            verifyInputsValidMethod = frame.getClass().getDeclaredMethod("verifyInputs", boolean.class);
//        } catch (java.lang.NoSuchMethodException nsm) {
//            Assert.fail("Could not find method verifyInputsValid in layoutEditorToolsFrame class ");
//        }
//
//        // override the default permissions.
//        Assert.assertNotNull(verifyInputsValidMethod);
//        verifyInputsValidMethod.setAccessible(true);
//        try {
//            int valid = (int) verifyInputsValidMethod.invoke(frame, false);
//            Assert.assertEquals("Expected Valid Sensors", 2, valid);
//        } catch (java.lang.IllegalAccessException ite) {
//            Assert.fail("could not access method verifyInputsValid in layoutEditorToolsFrame class");
//        } catch (java.lang.reflect.InvocationTargetException ite) {
//            Throwable cause = ite.getCause();
//            Assert.fail("verifyInputsValid execution failed reason: " + cause.getMessage());
//        }
    }

    @Test
    public void testVerifyInputsInValid() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
//        layoutEditorToolsFrame frame = new layoutEditorToolsFrame();
//        // don't set any values in the inputs.
//        // then use reflection to call verifyInputs
//        java.lang.reflect.Method verifyInputsValidMethod = null;
//        try {
//            verifyInputsValidMethod = frame.getClass().getDeclaredMethod("verifyInputs", boolean.class);
//        } catch (java.lang.NoSuchMethodException nsm) {
//            Assert.fail("Could not find method verifyInputsValid in layoutEditorToolsFrame class ");
//        }
//
//        // override the default permissions.
//        Assert.assertNotNull(verifyInputsValidMethod);
//        verifyInputsValidMethod.setAccessible(true);
//        try {
//            int valid = (int) verifyInputsValidMethod.invoke(frame, false);
//            Assert.assertEquals("Expected Valid Sensors", 0, valid);
//        } catch (java.lang.IllegalAccessException ite) {
//            Assert.fail("could not access method verifyInputsValid in layoutEditorToolsFrame class");
//        } catch (java.lang.reflect.InvocationTargetException ite) {
//            Throwable cause = ite.getCause();
//            Assert.fail("verifyInputsValid execution failed reason: " + cause.getMessage());
//        }
//        jmri.util.JUnitAppender.assertErrorMessage("Start sensor invalid:");
    }

    @Test
    public void testStartButton() {
        // this test only checks to see that we don't throw an exception when
        // pressing the buttons and all information is filled in.
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
//        layoutEditorToolsFrame frame = new layoutEditorToolsFrame();
//        frame.setVisible(true);
//        layoutEditorToolsScaffold operator = new layoutEditorToolsScaffold();
//        operator.setStartSensorValue("IS1");
//        operator.setStopSensor1Value("IS2");
//        operator.setDistance1Value("200");
//        operator.setStopSensor2Value("IS3");
//        operator.setDistance2Value("400");
//        operator.pushStartButton();
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    // private final static Logger log = LoggerFactory.getLogger(layoutEditorToolsFrameTest.class.getName());
}   // class LayoutEditorToolsFrameTest
