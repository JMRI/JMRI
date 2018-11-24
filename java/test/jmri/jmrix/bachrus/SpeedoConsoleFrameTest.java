package jmri.jmrix.bachrus;

import java.awt.GraphicsEnvironment;
import org.netbeans.jemmy.operators.JFrameOperator;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SpeedoConsoleFrameTest {
        
    private SpeedoConsoleFrame frame = null;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        Assert.assertNotNull("exists",frame);
    }

    @Test
    public void testShowAndClose() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        frame.initComponents();
        jmri.util.ThreadingUtil.runOnLayout( () ->{ frame.setVisible(true); });
        new JFrameOperator(frame.title()).requestClose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        JUnitUtil.initDebugThrottleManager();
        if( !GraphicsEnvironment.isHeadless()) { 
           SpeedoSystemConnectionMemo m = new SpeedoSystemConnectionMemo();
           SpeedoTrafficController tc = new SpeedoTrafficController(m);
           m.setSpeedoTrafficController(tc);
           frame = new SpeedoConsoleFrame(m);
        }
    }

    @After
    public void tearDown() {
        frame = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SpeedoConsoleFrameTest.class);

}
