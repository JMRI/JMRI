package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import jmri.util.JUnitOperationsUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ShowTrainsServingLocationFrameTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ShowTrainsServingLocationFrame t = new ShowTrainsServingLocationFrame();
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitOperationsUtil.resetOperationsManager();
        JUnitOperationsUtil.initOperationsData();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ShowTrainsServingLocationFrameTest.class);

}
