package jmri.jmrit.operations.trains;

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
public class TrainRoadOptionsActionTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");
        TrainEditFrame tf = new TrainEditFrame(train1);
        TrainRoadOptionsAction t = new TrainRoadOptionsAction("Test Action", tf);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(tf);
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

    // private final static Logger log = LoggerFactory.getLogger(TrainRoadOptionsActionTest.class);
}
