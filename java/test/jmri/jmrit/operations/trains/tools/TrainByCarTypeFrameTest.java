package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
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
    
    @Test
    public void testTrainByCarTypeFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train = tmanager.getTrainByName("Test Train Name");
        TrainByCarTypeFrame f = new TrainByCarTypeFrame();
        f.initComponents(train);

        Assert.assertNotNull("frame exists", f);
        JUnitUtil.dispose(f);
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

    // private final static Logger log = LoggerFactory.getLogger(TrainByCarTypeFrameTest.class);

}
