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
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of layoutEditorToolsFrame
 *
 * @author	George Warner Copyright (C) 2017
 */
public class LayoutEditorToolsFrameTest {

    //@SuppressWarnings("unchecked")
    public LayoutEditor openLayout() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        try {
            jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
            };
            Assert.assertNotNull("ConfigXmlManager exists", cm);

            // load and display sample file
            java.io.File leFile = new java.io.File("java/test/jmri/jmrit/display/layoutEditor/valid/SimpleLayoutEditorTest.xml");
            cm.load(leFile);
            sleep(100); // time for internal listeners to calm down
        } catch (Exception ex) {
            Logger.getLogger(LayoutEditorToolsFrameTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Find new window by name (should be more distinctive, comes from sample file)
        return (LayoutEditor) jmri.util.JmriJFrame.getFrame("My Layout");
    }

//    @Test
//    public void testCtor() {
//        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
//
//        LayoutEditor le = openLayout();
//        Assert.assertNotNull("LayoutEditor exists", le);
//    }

    @Test
    public void testPositionablePointPopup() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        LayoutEditor le = openLayout();
        Assert.assertNotNull("LayoutEditor exists", le);

        LayoutEditorFindItems leFinder = le.getFinder();
        Assert.assertNotNull("LayoutEditorFindItems exists", leFinder);

        // It's up at this point, and can be manipulated
        // make it editable
        le.setAllEditable(true);
        Assert.assertTrue("isEditable after setAllEditable(true)", le.isEditable());

        // setHighlightSelectedBlock(true)
        le.setHighlightSelectedBlock(true);
        Assert.assertTrue("getHighlightSelectedBlockafter after setHighlightSelectedBlock(true)", le.getHighlightSelectedBlock());

        // find End Bumper
        PositionablePoint ppEB1 = leFinder.findPositionablePointByName("EB1");
        Assert.assertNotNull("End Bumper EB1 exists", ppEB1);
        java.awt.Point location = MathUtil.PointForPoint2D(MathUtil.center(ppEB1.getBounds()));

        //
        //
        //
        //
        //
        //
        //TODO: comment out for production;
        // only here so developer can see what's happening
        try {
            sleep(3000);
        } catch (InterruptedException ex) {
            Logger.getLogger(LayoutEditorToolsFrameTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        JUnitUtil.dispose(le);
    }

    @Test
    public void testSetInputs() {
        // this test only checks to see that we don't throw an exception when
        // setting the input values.
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
//        layoutEditorToolsFrame frame = new layoutEditorToolsFrame();
//        frame.setInputs("IS1", "IS2", "IS3", "5280", "5280");
//        JUnitUtil.dispose(frame);
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
//        JUnitUtil.dispose(frame);
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
//        JUnitUtil.dispose(frame);
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
//        JUnitUtil.dispose(frame);
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
