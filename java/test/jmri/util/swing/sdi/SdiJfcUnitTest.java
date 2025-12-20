package jmri.util.swing.sdi;

import javax.swing.JButton;
import javax.swing.JFrame;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;
import jmri.util.swing.JmriNamedPaneAction;
import jmri.util.swing.SamplePane;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Swing tests for the SDI GUI.
 *
 * @author Bob Jacobsen Copyright 2010, 2015
 */
public class SdiJfcUnitTest {

    @Test
    @DisabledIfHeadless
    public void testShowAndClose() {

        JmriNamedPaneAction a = new JmriNamedPaneAction("Action",
                new JmriJFrameInterface(),
                jmri.util.swing.SamplePane.class.getName());

        a.actionPerformed(null);

        JFrame f1 = jmri.util.JmriJFrame.getFrame("SamplePane 1");
        assertNotNull( f1, "found frame 1");

        // Find the button that opens another panel
        JButton button = JButtonOperator.findJButton(f1, "Next1", true, true);
        assertNotNull(button);

        // Click it and check for next frame
        new JButtonOperator(button).doClick();

        JFrame f2 = jmri.util.JmriJFrame.getFrame("SamplePane 2");
        assertNotNull( f2, "found frame 2");

        // Close 2 directly
        new JFrameOperator(f2).dispose();
        new QueueTool().waitEmpty();
        assertEquals( 1, SamplePane.getDisposedList().size(), "one pane disposed");
        assertEquals( Integer.valueOf(2), SamplePane.getDisposedList().get(0), "pane 2 disposed");
        f2 = jmri.util.JmriJFrame.getFrame("SamplePane 2");
        assertNull( f2, "frame 2 is no longer visible");

        // Close 1 directly
        new JFrameOperator(f1).dispose();
        new QueueTool().waitEmpty();
        assertEquals( 2, SamplePane.getDisposedList().size(), "one pane disposed");
        assertEquals( Integer.valueOf(1), SamplePane.getDisposedList().get(1), "pane 1 disposed");
        f1 = jmri.util.JmriJFrame.getFrame("SamplePane 1");
        assertNull( f1, "frame 1 is no longer visible");

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
