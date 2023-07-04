package jmri.jmrix.bachrus;

import org.netbeans.jemmy.operators.JFrameOperator;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class SpeedoDialTest {
    
    private SpeedoDial dial = null;

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",dial);
    }

    @Test
    public void testShowInMph() {
        // this test is really just making sure there are no exceptions
        // when we draw the dial.  Most of the code in this class is in 
        // the overriden paint method.
        jmri.util.JmriJFrame frame = new jmri.util.JmriJFrame("SpeedoDial test mph frame");
        frame.getContentPane().setPreferredSize(new java.awt.Dimension(600, 300));
        frame.getContentPane().add(dial);
        frame.pack();
        frame.setVisible(true);
        dial.setUnitsMph();
        dial.reset();
        jmri.util.JUnitUtil.waitFor(() -> { return dial.isVisible(); },"dial visible");
        JFrameOperator jfo = new JFrameOperator(frame.getTitle());
        dial.update(10f);
        jfo.getQueueTool().waitEmpty();
        dial.update(20f);
        jfo.getQueueTool().waitEmpty();
        dial.update(30f);
        jfo.getQueueTool().waitEmpty();
        dial.update(40f);
        jfo.getQueueTool().waitEmpty();
        dial.update(50f);
        jfo.getQueueTool().waitEmpty();
        dial.update(60f);
        jfo.getQueueTool().waitEmpty();
        dial.update(1000f);
        jfo.getQueueTool().waitEmpty();

        jfo.requestClose();
        jfo.waitClosed();
        JUnitUtil.dispose(frame);
    }

    @Test
    public void testShowInKmph() {
        // this test is really just making sure there are no exceptions
        // when we draw the dial.  Most of the code in this class is in 
        // the overriden paint method.
        jmri.util.JmriJFrame frame = new jmri.util.JmriJFrame("SpeedoDial test kmph frame");
        frame.getContentPane().setPreferredSize(new java.awt.Dimension(123, 321));
        frame.getContentPane().add(dial);
        frame.pack();
        frame.setVisible(true);
        dial.setUnitsKph();
        dial.reset();
        jmri.util.JUnitUtil.waitFor(() -> { return dial.isVisible(); },"dial visible");
        JFrameOperator jfo = new JFrameOperator(frame.getTitle());
        dial.update(10f);
        jfo.getQueueTool().waitEmpty();
        dial.update(20f);
        jfo.getQueueTool().waitEmpty();
        dial.update(30f);
        jfo.getQueueTool().waitEmpty();
        dial.update(40f);
        jfo.getQueueTool().waitEmpty();
        dial.update(50f);
        jfo.getQueueTool().waitEmpty();
        dial.update(60f);
        jfo.getQueueTool().waitEmpty();
        dial.update(1000f);
        jfo.getQueueTool().waitEmpty();

        jfo.requestClose();
        jfo.waitClosed();
        JUnitUtil.dispose(frame);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        dial = new SpeedoDial();
    }

    @AfterEach
    public void tearDown() {
        dial = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SpeedoDialTest.class);

}
