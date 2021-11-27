package jmri.jmrix.openlcb.swing.tie;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.can.swing.tie.TieToolFrame class
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class TieToolFrameTest {

    @Test
    public void testCreateAndShow() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        jmri.util.JmriJFrame f = new TieToolFrame();
        f.initComponents();
        f.pack();

        f.setVisible(true);

        // close frame
        f.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }
}
