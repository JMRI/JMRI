package jmri.jmrit.operations.trains;

import java.io.File;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.JUnitOperationsUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TrainLoggerTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        TrainLogger t = new TrainLogger();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testLogging() {
        TrainLogger t = new TrainLogger();
        Assert.assertNotNull("exists",t);
        
        JUnitOperationsUtil.initOperationsData();
        // Turn on train logging
        Setup.setTrainLoggerEnabled(true);
        
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        
        Train train = tmanager.getTrainByName("STF");
        train.build();
        train.terminate();
        
        // confirm file created
        File file = new File(t.getFullLoggerFileName());
        Assert.assertTrue("Confirm file creation", file.exists()); 
        
        Setup.setTrainLoggerEnabled(false);
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

    // private final static Logger log = LoggerFactory.getLogger(TrainLoggerTest.class);

}
