package jmri.jmrit.operations.trains.tools;

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
public class TrainByCarTypeFrameTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.jmrit.operations.trains.Train train = jmri.InstanceManager.getDefault(jmri.jmrit.operations.trains.TrainManager.class).getTrainById("1");
        TrainByCarTypeFrame t = new TrainByCarTypeFrame();
        t.initComponents(train);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
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

    // private final static Logger log = LoggerFactory.getLogger(TrainByCarTypeFrameTest.class);

}
