package jmri.util.swing.sdi;

import java.awt.GraphicsEnvironment;
import javax.swing.JButton;
import javax.swing.JFrame;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.ButtonTestAction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Invokes complete set of tests in the jmri.util.swing.sdi tree
 *
 * @author	Bob Jacobsen Copyright 2010
 */
public class SdiWindowTest {

    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JmriJFrame f = new JmriJFrame("SDI test");
        JButton b = new JButton(new ButtonTestAction(
                "new frame", new jmri.util.swing.sdi.JmriJFrameInterface()));
        f.add(b);
        f.pack();
        f.setVisible(true);
        JFrame f2 = jmri.util.JmriJFrame.getFrame("SDI test");
        Assert.assertTrue("found frame", f2 != null);
        JUnitUtil.dispose(f2);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
