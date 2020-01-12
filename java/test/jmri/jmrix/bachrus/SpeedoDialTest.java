package jmri.jmrix.bachrus;

import java.awt.GraphicsEnvironment;
import org.netbeans.jemmy.operators.JFrameOperator;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SpeedoDialTest {
    
    private SpeedoDial dial = null;

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",dial);
    }

    @Test
    public void testShow() {
        // this test is really just making sure there are no exceptions
        // when we draw the dial.  Most of the code in this class is in 
        // the overriden paint method.
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.util.JmriJFrame frame = new jmri.util.JmriJFrame("SpeedoDial test frame");
        frame.getContentPane().setPreferredSize(new java.awt.Dimension(600, 300));
        frame.getContentPane().add(dial);
        frame.pack();
        frame.setVisible(true);
        dial.reset();
        jmri.util.JUnitUtil.waitFor(() -> { return dial.isVisible(); },"dial visible");
        new JFrameOperator("SpeedoDial test frame").requestClose();
        JUnitUtil.dispose(frame);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        dial = new SpeedoDial();
    }

    @After
    public void tearDown() {
        dial = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SpeedoDialTest.class);

}
