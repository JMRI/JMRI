package jmri.jmrit.operations.routes;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitOperationsUtil;
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
public class SetTrainIconRouteFrameTest {

    @Test
    public void testCTorNull() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SetTrainIconRouteFrame t = new SetTrainIconRouteFrame(null);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
    }

    @Test
    public void testCTorRoute() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SetTrainIconRouteFrame t = new SetTrainIconRouteFrame("Southbound Main Route");
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitOperationsUtil.resetOperationsManager();
        JUnitOperationsUtil.initOperationsData();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SetTrainIconRouteFrameTest.class);

}
