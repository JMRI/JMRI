package jmri.util.swing.multipane;

import java.awt.GraphicsEnvironment;
import javax.swing.JButton;
import javax.swing.JFrame;
import jmri.util.JUnitUtil;
import jmri.util.swing.SamplePane;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Swing jfcUnit tests for the Multipane (IDE) GUI
 *
 * @author	Bob Jacobsen Copyright 2010
 */
public class MultiJfcUnitTest {

    @Test
    public void testShow() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // show the window
        JFrame f1 = new MultiPaneWindow("test",
                "java/test/jmri/util/swing/xml/Gui3LeftTree.xml",
                "java/test/jmri/util/swing/xml/Gui3Menus.xml",
                "java/test/jmri/util/swing/xml/Gui3MainToolBar.xml"
        );
        f1.setSize(new java.awt.Dimension(500, 500));
        f1.setVisible(true);

        Assert.assertNotNull("found main frame", f1);

        // Load the license
        JUnitUtil.pressButton(f1, "License");

        // Find the button that opens a sample panel
        JButton samplebutton = JButtonOperator.findJButton(f1, "Sample", true, true);
        Assert.assertNotNull("Sample button found", samplebutton);
        JButtonOperator sampleOperator = new JButtonOperator(samplebutton);

        // Click the sample button to load new pane over license
        sampleOperator.doClick();

        // Find the button on new panel
        JButton next1button = JButtonOperator.findJButton(f1, "Next1", true, true);
        Assert.assertNotNull("Next1 button found", next1button);

        // Click it to load new window with Next2
        new JButtonOperator(next1button).doClick();

        // nobody disposed yet
        Assert.assertEquals("no panes disposed", 0, SamplePane.disposed.size());

        // Find the Next2 button on new panel
        JButton next2button = JButtonOperator.findJButton(f1, "Next2", true, true);
        Assert.assertNotNull("Next2 button found", next2button);

        // Click sample to reload 0 pane over 1 pane
        sampleOperator.doClick();

        // Find the button on restored panel
        JButton button = JButtonOperator.findJButton(f1, "Next1", true, true);
        Assert.assertEquals("found same pane", next1button, button);

        // Find the button to open a pane in lower window
        JButton extendButton = JButtonOperator.findJButton(f1, "Extend1", true, true);
        Assert.assertNotNull("Extend1 button found", extendButton);
        // Press it
        new JButtonOperator(extendButton).doClick();

        // Both Close1 and Close3 should be present
        button = JButtonOperator.findJButton(f1, "Close1", true, true);
        Assert.assertNotNull("Closee1 button found", button);
        button = JButtonOperator.findJButton(f1, "Close3", true, true);
        Assert.assertNotNull("Close3 button found", button);

        // nobody disposed yet
        Assert.assertEquals("no panes disposed", 0, SamplePane.disposed.size());

        // Close entire frame directly
        new JFrameOperator(f1).dispose();

        // Now they're disposed
        Assert.assertEquals("panes disposed", 3, SamplePane.disposed.size());

    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        jmri.util.swing.SamplePane.disposed = new java.util.ArrayList<>();
        jmri.util.swing.SamplePane.index = 0;
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
