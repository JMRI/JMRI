package jmri.util.swing.sdi;

import javax.swing.JButton;
import javax.swing.JFrame;

import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.junit.annotations.DisabledIfHeadless;
import jmri.util.swing.ButtonTestAction;

import org.junit.jupiter.api.*;

/**
 * Invokes complete set of tests in the jmri.util.swing.sdi tree
 *
 * @author Bob Jacobsen Copyright 2010
 */
public class SdiWindowTest {

    @Test
    @DisabledIfHeadless
    public void testAction() {
        JmriJFrame f = new JmriJFrame("SDI test");
        JButton b = new JButton(new ButtonTestAction(
                "new frame", new jmri.util.swing.sdi.JmriJFrameInterface()));
        f.add(b);
        f.pack();
        f.setVisible(true);
        JFrame f2 = jmri.util.JmriJFrame.getFrame("SDI test");
        Assertions.assertNotNull( f2, "found frame");
        JUnitUtil.dispose(f2);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
