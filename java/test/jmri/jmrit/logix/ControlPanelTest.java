package jmri.jmrit.logix;

import java.awt.GraphicsEnvironment;
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
public class ControlPanelTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LearnThrottleFrame f = new LearnThrottleFrame(new WarrantFrame(new Warrant("IW0", "AllTestWarrant")));
        ControlPanel t = new ControlPanel(f);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(f);
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

    // private final static Logger log = LoggerFactory.getLogger(ControlPanelTest.class);

}
