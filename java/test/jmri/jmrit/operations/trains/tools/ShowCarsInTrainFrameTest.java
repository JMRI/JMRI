package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
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
public class ShowCarsInTrainFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ShowCarsInTrainFrame t = new ShowCarsInTrainFrame();
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
    }
    
    @Test
    public void testFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ShowCarsInTrainFrame t = new ShowCarsInTrainFrame();
        Assert.assertNotNull("exists",t);
        
        JUnitOperationsUtil.initOperationsData();
        
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train = tmanager.getTrainByName("STF");
        t.initComponents(train);
        
        Assert.assertTrue(t.isShowing());
        
        // next should update window
        Assert.assertTrue(train.build());
        
        JUnitUtil.dispose(t);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        super.setUp();
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ShowCarsInTrainFrameTest.class);

}
