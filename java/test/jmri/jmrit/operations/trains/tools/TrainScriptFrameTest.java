package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainEditFrame;
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
public class TrainScriptFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train = tmanager.getTrainByName("STF");
        
        TrainScriptFrame t = new TrainScriptFrame();
        TrainEditFrame tef = new TrainEditFrame(train);
        Assert.assertNotNull("exists",t);
        t.initComponents(tef);
        JUnitUtil.dispose(t);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        super.setUp();
 
        JUnitOperationsUtil.initOperationsData();
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainScriptFrameTest.class);

}
