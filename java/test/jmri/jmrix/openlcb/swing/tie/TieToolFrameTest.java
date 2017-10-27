package jmri.jmrix.openlcb.swing.tie;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.can.swing.tie.TieToolFrame class
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class TieToolFrameTest {

    @Test
    public void testCreateAndShow() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        jmri.util.JmriJFrame f = new TieToolFrame();
        f.initComponents();
        f.pack();

        f.setVisible(true);

        // close frame
        f.dispose();
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
