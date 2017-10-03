package jmri.jmrit.operations.routes;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SetTrainIconRouteActionTest {

    @Test
    public void testCTor() {
        SetTrainIconRouteAction t = new SetTrainIconRouteAction("Test");
        Assert.assertNotNull("exists",t);
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

    // private final static Logger log = LoggerFactory.getLogger(SetTrainIconRouteActionTest.class);

}
