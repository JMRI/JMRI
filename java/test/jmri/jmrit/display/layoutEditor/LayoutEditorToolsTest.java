package jmri.jmrit.display.layoutEditor;

import static java.lang.Thread.sleep;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JDialog;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.util.JUnitUtil;
import jmri.util.swing.JmriBeanComboBox;
import junit.extensions.jfcunit.finder.ComponentFinder;
import junit.extensions.jfcunit.finder.DialogFinder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

/**
 * Test simple functioning of LayoutEditorTools
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LayoutEditorToolsTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

      jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };
        Assert.assertNotNull("ConfigXmlManager exists", cm);

        // load and display sample file
        java.io.File leFile = new java.io.File("java/test/jmri/jmrit/display/layoutEditor/valid/SimpleLayoutEditorTest.xml");
        try {
            cm.load(leFile);
        } catch (JmriConfigureXmlException ex) {
            Assert.assertTrue("Able to load file", false);
        }
        try {
            sleep(100); // time for internal listeners to calm down
        } catch (InterruptedException ex) {
        }

        // Find new window by name (should be more distinctive, comes from sample file)
        LayoutEditor le = (LayoutEditor) jmri.util.JmriJFrame.getFrame("My Layout");
        Assert.assertNotNull("LayoutEditor exists", le);

        JComponent leTargetPane = le.getTargetPanel();
        Assert.assertNotNull("Target Panel exists", leTargetPane);
        dumpComponent(leTargetPane);

        LayoutEditorFindItems leFinder = le.getFinder();
        Assert.assertNotNull("LayoutEditorFindItems exists", leFinder);

        // It's up at this point, and can be manipulated
        // make it editable
        le.setAllEditable(true);
        Assert.assertTrue("isEditable after setAllEditable(true)", le.isEditable());

        LayoutEditorTools leTools = le.getLETools();
        Assert.assertNotNull("exists", leTools);

        // find Anchor A4
        PositionablePoint ppA4 = leFinder.findPositionablePointByName("A4");
        Assert.assertNotNull("Anchor A4 exists", ppA4);

        leTools.setSignalsAtBlockBoundary(le.signalIconEditor, le);

        String title = Bundle.getMessage("SignalsAtBoundary");
        DialogFinder df = new DialogFinder(title);
        List<JDialog> dialogList = df.findAll();
        Assert.assertNotNull("dialogList exists", dialogList);
        System.out.println("dialogList.size(): " + dialogList.size());
        for (JDialog dialog : dialogList) {
            dumpComponent(dialog);
        }

        ComponentFinder cf = new ComponentFinder(JmriBeanComboBox.class);
        List<JmriBeanComboBox> comboBoxes = cf.findAll();
        Assert.assertNotNull("comboBoxes exists", comboBoxes);
        System.out.println("comboBoxes.size(): " + comboBoxes.size());

        for (JmriBeanComboBox comboBox : comboBoxes) {
            dumpComponent(comboBox);
        }

        //
        //
        //
        //TODO: comment out for production; 
        // only here so developer can see what's happening
//        try {
//            sleep(300);
//        } catch (InterruptedException ex) {
//        }

        //
        //
        //
        le.dispose();
    }

    // routine to dump component hierarchy
    private void dumpComponent(Component inComponent) {
        dumpComponent(inComponent, 0);
    }

    private void dumpComponent(Component inComponent, int inDepth) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < inDepth; i++) {
            builder.append("|   ");
        }
        String prefix = builder.toString();

        String label = "";
        Method methodToFind = null;
        try {
            methodToFind = inComponent.getClass().getMethod("getLabel", (Class<?>[]) null);
            if (methodToFind != null) {
                try {
                    // Method found. You can invoke the method like
                    label = (String) methodToFind.invoke(inComponent, (Object[]) null);
                } catch (IllegalAccessException ex) {
                    //log.error("invoke ", ex);
                } catch (IllegalArgumentException ex) {
                    //log.error("invoke ", ex);
                } catch (InvocationTargetException ex) {
                    //log.error("invoke ", ex);
                }
            }
        } catch (NoSuchMethodException | SecurityException ex) {
            //log.error("getMethod('getLabel') ", ex);
        }

        System.out.println(prefix + inComponent.getClass().getName() + ":" + label);

        if (inComponent instanceof Container) {
            Component[] clist = ((Container) inComponent).getComponents();
            for (Component c : clist) {
                dumpComponent(c, inDepth + 1);
            }
        }
    }

    // from here down is testing infrastructure
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

    //initialize logging
    private final static org.slf4j.Logger log = LoggerFactory.getLogger(LayoutEditorToolsTest.class.getName());
}
