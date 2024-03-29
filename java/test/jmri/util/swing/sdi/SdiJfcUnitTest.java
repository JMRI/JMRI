package jmri.util.swing.sdi;

import javax.swing.JButton;
import javax.swing.JFrame;

import jmri.util.JUnitUtil;
import jmri.util.swing.JmriNamedPaneAction;
import jmri.util.swing.SamplePane;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Swing tests for the SDI GUI.
 *
 * @author Bob Jacobsen Copyright 2010, 2015
 */
public class SdiJfcUnitTest {

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testShowAndClose() throws Exception {

        JmriNamedPaneAction a = new JmriNamedPaneAction("Action",
                new JmriJFrameInterface(),
                jmri.util.swing.SamplePane.class.getName());

        a.actionPerformed(null);

        JFrame f1 = jmri.util.JmriJFrame.getFrame("SamplePane 1");
        Assert.assertTrue("found frame 1", f1 != null);

        // Find the button that opens another panel
        JButton button = JButtonOperator.findJButton(f1, "Next1", true, true);
        Assert.assertNotNull(button);

        // Click it and check for next frame
        new JButtonOperator(button).doClick();

        JFrame f2 = jmri.util.JmriJFrame.getFrame("SamplePane 2");
        Assert.assertTrue("found frame 2", f2 != null);

        // Close 2 directly
        new JFrameOperator(f2).dispose();
        new QueueTool().waitEmpty();
        Assert.assertEquals("one pane disposed", 1, SamplePane.getDisposedList().size());
        Assert.assertEquals("pane 2 disposed", Integer.valueOf(2), SamplePane.getDisposedList().get(0));
        f2 = jmri.util.JmriJFrame.getFrame("SamplePane 2");
        Assert.assertTrue("frame 2 is no longer visible", f2 == null);

        // Close 1 directly
        new JFrameOperator(f1).dispose();
        new QueueTool().waitEmpty();
        Assert.assertEquals("one pane disposed", 2, SamplePane.getDisposedList().size());
        Assert.assertEquals("pane 1 disposed", Integer.valueOf(1), SamplePane.getDisposedList().get(1));
        f1 = jmri.util.JmriJFrame.getFrame("SamplePane 1");
        Assert.assertTrue("frame 1 is no longer visible", f1 == null);

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();

        SamplePane.resetCounts();

    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
