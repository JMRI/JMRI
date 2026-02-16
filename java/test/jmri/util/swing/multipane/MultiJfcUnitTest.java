package jmri.util.swing.multipane;

import javax.swing.JButton;
import javax.swing.JFrame;

import jmri.util.JUnitUtil;
import jmri.util.swing.SamplePane;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Swing tests for the Multipane (IDE) GUI
 *
 * @author Bob Jacobsen Copyright 2010
 */
public class MultiJfcUnitTest {

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    public void testShow() {

        // show the window
        JFrame f1 = new MultiPaneWindow("test",
                "java/test/jmri/util/swing/xml/Gui3LeftTree.xml",
                "java/test/jmri/util/swing/xml/Gui3Menus.xml",
                "java/test/jmri/util/swing/xml/Gui3MainToolBar.xml"
        );
        f1.setSize(new java.awt.Dimension(500, 500));
        f1.setVisible(true);

        assertNotNull( f1, "found main frame");

        // Load the license
        JUnitUtil.pressButton(f1, "License");
        JFrameOperator jfl = new JFrameOperator("JMRI License");
        jfl.requestClose();
        jfl.waitClosed();        

        // Find the button that opens a sample panel
        JButton samplebutton = JButtonOperator.findJButton(f1, "Sample", true, true);
        assertNotNull( samplebutton, "Sample button found");
        JButtonOperator sampleOperator = new JButtonOperator(samplebutton);

        // Click the sample button to load new pane over license
        sampleOperator.doClick();

        // Find the button on new panel
        JButton next1button = JButtonOperator.findJButton(f1, "Next1", true, true);
        assertNotNull( next1button, "Next1 button found");

        // Click it to load new window with Next2
        new JButtonOperator(next1button).doClick();

        // nobody disposed yet
        assertEquals( 0, SamplePane.getDisposedList().size(), "no panes disposed");

        // Find the Next2 button on new panel
        JButton next2button = JButtonOperator.findJButton(f1, "Next2", true, true);
        assertNotNull( next2button, "Next2 button found");

        // Click sample to reload 0 pane over 1 pane
        sampleOperator.doClick();

        // Find the button on restored panel
        JButton button = JButtonOperator.findJButton(f1, "Next1", true, true);
        assertEquals( next1button, button, "found same pane");

        // Find the button to open a pane in lower window
        JButton extendButton = JButtonOperator.findJButton(f1, "Extend1", true, true);
        assertNotNull( extendButton, "Extend1 button found");
        // Press it
        new JButtonOperator(extendButton).doClick();

        // Both Close1 and Close3 should be present
        button = JButtonOperator.findJButton(f1, "Close1", true, true);
        assertNotNull( button, "Closee1 button found");
        button = JButtonOperator.findJButton(f1, "Close3", true, true);
        assertNotNull( button, "Close3 button found");

        // nobody disposed yet
        assertEquals( 0, SamplePane.getDisposedList().size(), "no panes disposed");

        // Close entire frame directly
        new JFrameOperator(f1).dispose();

        // Now they're disposed
        assertEquals( 3, SamplePane.getDisposedList().size(), "panes disposed");

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        SamplePane.resetCounts();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
